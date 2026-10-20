package com.kairos.app.engine.capsule

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.webkit.MimeTypeMap
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.engine.notify.Scheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Seals letter text with an AES-GCM key that lives in the Android Keystore. Nothing in the app
 * decrypts a capsule before its unlock time — the lock is a promise the code keeps.
 */
object CapsuleCrypto {
    private const val ALIAS = "kairos_capsule_v1"
    private const val TRANSFORM = "AES/GCM/NoPadding"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }

    fun seal(plain: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val out = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(out, Base64.NO_WRAP) to Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
    }

    fun open(cipherText: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)), Charsets.UTF_8)
    }
}

enum class AttachmentKind { Image, Video, Audio, File }

data class DraftAttachment(
    val uri: Uri? = null,
    val file: File? = null,
    val name: String,
    val mime: String,
    val bytes: Long,
    val kind: AttachmentKind,
    val durationMs: Long = 0,
)

data class Capsule(
    val entity: CapsuleEntity,
    val attachments: List<AttachmentEntity>,
) {
    val id get() = entity.id
    fun isReady(now: Long) = now >= entity.unlockAt
    val isOpened get() = entity.openedAt != null
}

class CapsuleRepository(
    private val context: Context,
    private val db: KairosDatabase,
) {
    private val dao = db.capsules()

    fun observeAll(): Flow<List<CapsuleEntity>> = dao.observeAll()
    fun observe(id: String) = dao.observe(id)
    fun observeAttachments(id: String) = dao.observeAttachments(id)
    fun observeAllAttachments() = dao.observeAllAttachments()

    fun describe(uri: Uri): DraftAttachment {
        val resolver = context.contentResolver
        var name = "attachment"
        var size = 0L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getString(0)?.let { name = it }
                if (!c.isNull(1)) size = c.getLong(1)
            }
        }
        val mime = resolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())
            ?: "application/octet-stream"
        return DraftAttachment(uri = uri, name = name, mime = mime, bytes = size, kind = kindOf(mime))
    }

    fun kindOf(mime: String) = when {
        mime.startsWith("image/") -> AttachmentKind.Image
        mime.startsWith("video/") -> AttachmentKind.Video
        mime.startsWith("audio/") -> AttachmentKind.Audio
        else -> AttachmentKind.File
    }

    /** Copies attachments into private storage so they survive the source being deleted. */
    suspend fun seal(
        title: String,
        body: String,
        unlockAt: Long,
        mood: Int,
        stamp: Int,
        prompt: String,
        drafts: List<DraftAttachment>,
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val dir = File(context.filesDir, "capsules/$id").apply { mkdirs() }
        val attachments = drafts.mapIndexedNotNull { i, d ->
            val safe = d.name.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(60)
            val target = File(dir, "${i}_$safe")
            val ok = runCatching {
                when {
                    d.file != null -> d.file.copyTo(target, overwrite = true).also { d.file.delete() }
                    d.uri != null -> context.contentResolver.openInputStream(d.uri)?.use { input ->
                        target.outputStream().use { input.copyTo(it) }
                    }
                    else -> null
                }
            }.isSuccess && target.exists()
            if (!ok) return@mapIndexedNotNull null
            AttachmentEntity(
                id = UUID.randomUUID().toString(), capsuleId = id, path = target.absolutePath, mime = d.mime,
                name = d.name, bytes = target.length(), kind = d.kind.name, durationMs = d.durationMs,
            )
        }
        val (cipher, iv) = CapsuleCrypto.seal(body)
        val entity = CapsuleEntity(
            id = id, title = title.ifBlank { "A letter to future me" }, cipher = cipher, iv = iv,
            createdAt = System.currentTimeMillis(), unlockAt = unlockAt, mood = mood, stamp = stamp, prompt = prompt,
        )
        dao.insertWithAttachments(entity, attachments)
        Scheduler.scheduleCapsule(context, id, entity.title, unlockAt)
        id
    }

    /** Returns the letter text only once the capsule's moment has arrived. */
    suspend fun read(id: String): String? = withContext(Dispatchers.IO) {
        val c = dao.get(id) ?: return@withContext null
        if (System.currentTimeMillis() < c.unlockAt) return@withContext null
        runCatching { CapsuleCrypto.open(c.cipher, c.iv) }.getOrElse { "This letter could not be unsealed on this device." }
    }

    suspend fun markOpened(id: String) = dao.markOpened(id, System.currentTimeMillis())

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val c = dao.get(id) ?: return@withContext
        File(context.filesDir, "capsules/$id").deleteRecursively()
        dao.delete(c)
        Scheduler.cancelCapsule(context, id)
    }

    /** Re-arms unlock reminders (e.g. after an app update cleared WorkManager). */
    suspend fun rescheduleAll() {
        dao.sealed().forEach { Scheduler.scheduleCapsule(context, it.id, it.title, it.unlockAt) }
    }

    fun newVoiceFile(): File = File(context.cacheDir, "voice").apply { mkdirs() }.let { File(it, "voice_${System.currentTimeMillis()}.m4a") }
}
