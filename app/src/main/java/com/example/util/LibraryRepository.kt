package com.example.util

import com.example.data.ArticleCacheDao
import com.example.data.ArticleCacheEntity
import com.example.data.BookmarkDao
import com.example.data.BookmarkEntity
import com.example.data.HistoryDao
import com.example.data.HistoryEntity
import com.example.data.NoteDao
import com.example.data.NoteEntity
import com.example.data.ReadingItemDao
import com.example.data.ReadingItemEntity
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao,
    private val readingItemDao: ReadingItemDao,
    private val noteDao: NoteDao,
    private val articleCacheDao: ArticleCacheDao
) {
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarks()
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getHistory()
    val allReadingItems: Flow<List<ReadingItemEntity>> = readingItemDao.getAllReadingItems()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allSavedArticles: Flow<List<ArticleCacheEntity>> = articleCacheDao.getAllSavedArticles()

    suspend fun addBookmark(title: String, url: String) {
        bookmarkDao.insertBookmark(BookmarkEntity(title = title, url = url))
    }

    suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    suspend fun addHistory(title: String, url: String) {
        historyDao.insertHistory(HistoryEntity(title = title, url = url))
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun updateReadingStatus(url: String, title: String, status: String, doi: String? = null) {
        readingItemDao.insert(
            ReadingItemEntity(
                url = url,
                title = title,
                status = status,
                doi = doi,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addNote(url: String, pageTitle: String, selectedText: String, noteText: String) {
        noteDao.insert(
            NoteEntity(
                url = url,
                pageTitle = pageTitle,
                selectedText = selectedText,
                noteText = noteText,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

    suspend fun saveArticleOffline(url: String, title: String, content: String, author: String? = null) {
        articleCacheDao.saveArticle(
            ArticleCacheEntity(
                url = url,
                title = title,
                extractedContent = content,
                author = author,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getOfflineArticle(url: String): ArticleCacheEntity? {
        return articleCacheDao.getArticle(url)
    }

    suspend fun deleteOfflineArticle(url: String) {
        articleCacheDao.deleteArticle(url)
    }
}
