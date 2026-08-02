package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey val url: String,
    val summaryText: String,
    val tldrBullets: String,
    val simplifiedText: String,
    val suggestedQuestionsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
