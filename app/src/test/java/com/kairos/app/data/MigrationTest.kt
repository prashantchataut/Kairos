package com.kairos.app.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.MIGRATION_1_2
import com.kairos.app.data.db.MIGRATION_2_3
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Builds a real version-1 database from the exported schema, then opens it with the current app. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {
    @Test fun `version 1 data survives the moves to version 3`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val schema = Json.parseToJsonElement(File("schemas/com.kairos.app.data.db.KairosDatabase/1.json").readText()).jsonObject["database"]!!.jsonObject
        val file = context.getDatabasePath("migrate.db").also { it.parentFile?.mkdirs(); it.delete() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            schema["entities"]!!.jsonArray.forEach { e ->
                val o = e.jsonObject
                val table = o["tableName"]!!.jsonPrimitive.content
                db.execSQL(o["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                o["indices"]?.jsonArray?.forEach { db.execSQL(it.jsonObject["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table)) }
            }
            schema["setupQueries"]!!.jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
            db.execSQL("INSERT INTO words (id, word, partOfSpeech, pronunciation, definition, example, origin, synonyms, category, level, hook, generatedAt, saved, timesShown, taste, difficultyVote, reps, ease, intervalDays, lapses) VALUES ('apricity','Apricity','noun','','Winter sun.','','','','nature',3,'',1,1,1,0,0,0,2.5,0,0)")
            db.execSQL("INSERT INTO quotes (id, text, author, theme, note, generatedAt, saved, timesShown, taste) VALUES ('q1','Nature does not hurry.','Lao Tzu','calm','',1,0,0,0)")
            db.version = 1
        }
        val room = Room.databaseBuilder(context, KairosDatabase::class.java, "migrate.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3).allowMainThreadQueries().build()
        val word = room.words().get("apricity")!!
        assertEquals("", word.language)
        assertEquals(0L, word.dwellMs)
        assertTrue(word.saved)
        assertEquals("quote", room.quotes().get("q1")!!.kind)
        assertEquals(0, room.books().all().size)
        room.close()
    }
}
