package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_items")
data class ReadingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val status: String = "TO_READ", // TO_READ, READING, READ
    val doi: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
