package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val pageTitle: String,
    val selectedText: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)
