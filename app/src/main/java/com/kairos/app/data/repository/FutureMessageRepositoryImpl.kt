package com.kairos.app.data.repository

import com.kairos.app.data.local.dao.FutureMessageDao
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.domain.common.ErrorType
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.common.runSuspendCatching
import com.kairos.app.domain.repository.FutureMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.kairos.app.data.auth.UserIdProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FutureMessageRepositoryImpl @Inject constructor(
    private val dao: FutureMessageDao,
    private val user: UserIdProvider
) : FutureMessageRepository {
    override fun observeDeliveredMessages(): Flow<List<FutureMessageEntity>> = dao.getDeliveredMessages().map { rows -> rows.filter { it.userId == user.getCurrentUserId() && !it.isDeleted } }
    override fun observePendingMessages(): Flow<List<FutureMessageEntity>> = dao.getPendingMessages().map { rows -> rows.filter { it.userId == user.getCurrentUserId() && !it.isDeleted } }
    override fun observeUnreadCount(): Flow<Int> = observeDeliveredMessages().map { rows -> rows.count { !it.isRead } }

    override suspend fun getMessage(id: Long): Result<FutureMessageEntity> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not open this letter.") {
            dao.getMessageById(id) ?: throw NoSuchElementException("Future message $id was not found")
        }

    override suspend fun getMessagesReadyForDelivery(now: Long): Result<List<FutureMessageEntity>> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not check arriving letters.") {
            dao.getMessagesReadyForDelivery(now)
        }

    override suspend fun createMessage(message: FutureMessageEntity): Result<Long> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not seal this letter.") {
            dao.insertMessage(message)
        }

    override suspend fun markDelivered(id: Long, deliveredAt: Long): Result<Unit> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not deliver this letter.") {
            dao.markAsDelivered(id, deliveredAt)
        }

    override suspend fun markRead(id: Long): Result<Unit> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not update this letter.") {
            dao.markAsRead(id)
        }

    override suspend fun markRevealed(id: Long, readAt: Long): Result<Unit> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not update this letter.") {
            dao.markAsReadWithTimestamp(id, readAt)
        }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean): Result<Unit> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not update favorites.") {
            dao.setFavorite(id, isFavorite)
        }

    override suspend fun linkReplyJournal(id: Long, journalId: Long): Result<Unit> =
        runSuspendCatching(ErrorType.DATABASE, "Kairos could not link this reflection.") {
            dao.setReplyJournalEntry(id, journalId)
        }
}
