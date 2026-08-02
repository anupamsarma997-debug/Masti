package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingItemDao {
    @Query("SELECT * FROM reading_items ORDER BY timestamp DESC")
    fun getAllReadingItems(): Flow<List<ReadingItemEntity>>

    @Query("SELECT * FROM reading_items WHERE url = :url LIMIT 1")
    suspend fun getReadingItemByUrl(url: String): ReadingItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReadingItemEntity)

    @Update
    suspend fun update(item: ReadingItemEntity)

    @Query("DELETE FROM reading_items WHERE url = :url")
    suspend fun deleteByUrl(url: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE url = :url ORDER BY timestamp DESC")
    fun getNotesForUrl(url: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}

@Dao
interface AiCacheDao {
    @Query("SELECT * FROM ai_cache WHERE url = :url LIMIT 1")
    suspend fun getCache(url: String): AiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AiCacheEntity)
}

@Dao
interface ArticleCacheDao {
    @Query("SELECT * FROM article_cache ORDER BY savedAt DESC")
    fun getAllSavedArticles(): Flow<List<ArticleCacheEntity>>

    @Query("SELECT * FROM article_cache WHERE url = :url LIMIT 1")
    suspend fun getArticle(url: String): ArticleCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticle(article: ArticleCacheEntity)

    @Query("DELETE FROM article_cache WHERE url = :url")
    suspend fun deleteArticle(url: String)
}
