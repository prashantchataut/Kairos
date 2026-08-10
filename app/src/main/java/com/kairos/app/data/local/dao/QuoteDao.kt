package com.kairos.app.data.local.dao

import androidx.room.*
import com.kairos.app.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun countAll(): Int

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    suspend fun getAllQuotesSync(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE shownAsDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getQuoteOfTheDay(): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY RANDOM()")
    fun getQuotesByCategory(category: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE author = :author ORDER BY id ASC")
    fun getQuotesByAuthor(author: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE content LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    @Query("SELECT DISTINCT author FROM quotes ORDER BY author ASC")
    fun getAllAuthors(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM quotes")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Query("UPDATE quotes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE quotes SET shownAsDaily = 1, shownAt = :shownAt WHERE id = :id")
    suspend fun markAsShownDaily(id: Long, shownAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    // ==================== ACTIVE PROGRESS QUERIES ====================

    /**
     * Get count of saved/favorited quotes (for "Next Action" suggestions)
     */
    @Query("SELECT COUNT(*) FROM quotes WHERE isFavorite = 1")
    suspend fun getSavedQuoteCount(): Int
}
