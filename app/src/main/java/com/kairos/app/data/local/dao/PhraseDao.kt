package com.kairos.app.data.local.dao

import androidx.room.*
import com.kairos.app.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun countAll(): Int

    @Query("SELECT * FROM phrases ORDER BY phrase ASC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhraseById(id: Long): PhraseEntity?

    @Query("SELECT * FROM phrases WHERE shownAsDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getPhraseOfTheDay(): PhraseEntity?

    @Query("SELECT * FROM phrases WHERE isFavorite = 1 ORDER BY phrase ASC")
    fun getFavoritePhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE category = :category ORDER BY phrase ASC")
    fun getPhrasesByCategory(category: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE formality = :formality ORDER BY phrase ASC")
    fun getPhrasesByFormality(formality: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE phrase LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchPhrases(query: String): Flow<List<PhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)

    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)

    @Query("UPDATE phrases SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE phrases SET shownAsDaily = 1, shownAt = :shownAt WHERE id = :id")
    suspend fun markAsShownDaily(id: Long, shownAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
}