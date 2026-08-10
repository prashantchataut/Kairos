package com.kairos.app.data.local.dao

import androidx.room.*
import com.kairos.app.data.local.entity.ProverbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProverbDao {

    @Query("SELECT COUNT(*) FROM proverbs")
    suspend fun countAll(): Int

    @Query("SELECT * FROM proverbs ORDER BY id ASC")
    fun getAllProverbs(): Flow<List<ProverbEntity>>

    @Query("SELECT * FROM proverbs WHERE id = :id")
    suspend fun getProverbById(id: Long): ProverbEntity?

    @Query("SELECT * FROM proverbs WHERE shownAsDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getProverbOfTheDay(): ProverbEntity?

    @Query("SELECT * FROM proverbs WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteProverbs(): Flow<List<ProverbEntity>>

    @Query("SELECT * FROM proverbs WHERE category = :category ORDER BY RANDOM()")
    fun getProverbsByCategory(category: String): Flow<List<ProverbEntity>>

    @Query("SELECT * FROM proverbs WHERE origin = :origin ORDER BY id ASC")
    fun getProverbsByOrigin(origin: String): Flow<List<ProverbEntity>>

    @Query("SELECT * FROM proverbs WHERE content LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchProverbs(query: String): Flow<List<ProverbEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProverb(proverb: ProverbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProverbs(proverbs: List<ProverbEntity>)

    @Update
    suspend fun updateProverb(proverb: ProverbEntity)

    @Query("UPDATE proverbs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE proverbs SET shownAsDaily = 1, shownAt = :shownAt WHERE id = :id")
    suspend fun markAsShownDaily(id: Long, shownAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProverb(proverb: ProverbEntity)
}