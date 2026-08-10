package com.kairos.app.data.local.dao

import androidx.room.*
import com.kairos.app.data.local.entity.IdiomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdiomDao {

    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun countAll(): Int

    @Query("SELECT * FROM idioms ORDER BY phrase ASC")
    fun getAllIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE id = :id")
    suspend fun getIdiomById(id: Long): IdiomEntity?

    @Query("SELECT * FROM idioms WHERE shownAsDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getIdiomOfTheDay(): IdiomEntity?

    @Query("SELECT * FROM idioms WHERE isFavorite = 1 ORDER BY phrase ASC")
    fun getFavoriteIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE category = :category ORDER BY phrase ASC")
    fun getIdiomsByCategory(category: String): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE phrase LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchIdioms(query: String): Flow<List<IdiomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdiom(idiom: IdiomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdioms(idioms: List<IdiomEntity>)

    @Update
    suspend fun updateIdiom(idiom: IdiomEntity)

    @Query("UPDATE idioms SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE idioms SET shownAsDaily = 1, shownAt = :shownAt WHERE id = :id")
    suspend fun markAsShownDaily(id: Long, shownAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteIdiom(idiom: IdiomEntity)
}