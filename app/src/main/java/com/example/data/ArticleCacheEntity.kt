package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article_cache")
data class ArticleCacheEntity(
    @PrimaryKey val url: String,
    val title: String,
    val extractedContent: String,
    val author: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)
