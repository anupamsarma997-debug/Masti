package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ArticleCacheEntity
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.data.NoteEntity
import com.example.data.ReadingItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryDialog(

    bookmarks: List<BookmarkEntity>,
    history: List<HistoryEntity>,
    readingItems: List<ReadingItemEntity>,
    notes: List<NoteEntity>,
    savedArticles: List<ArticleCacheEntity>,
    onSelectUrl: (String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onClearHistory: () -> Unit,
    onUpdateReadingStatus: (String, String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onOpenOfflineArticle: (ArticleCacheEntity) -> Unit,
    onDeleteOfflineArticle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var readingFilter by remember { mutableStateOf("ALL") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Research Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Reading List") },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Notes (${notes.size})") },
                        icon = { Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Offline (${savedArticles.size})") },
                        icon = { Icon(Icons.Default.OfflinePin, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Bookmarks") },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("History") },
                        icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> {
                            // Reading List with Filters
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                listOf("ALL", "TO_READ", "READING", "READ").forEach { filter ->
                                    val isSelected = readingFilter == filter
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { readingFilter = filter }
                                    ) {
                                        Text(
                                            text = filter.replace("_", " "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            val filteredItems = remember(readingItems, readingFilter) {
                                if (readingFilter == "ALL") readingItems
                                else readingItems.filter { it.status == readingFilter }
                            }

                            if (filteredItems.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No items in reading list.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filteredItems) { item ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onSelectUrl(item.url)
                                                onDismiss()
                                            }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(item.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    listOf("TO_READ", "READING", "READ").forEach { st ->
                                                        val isCur = item.status == st
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                            modifier = Modifier.clickable {
                                                                onUpdateReadingStatus(item.url, item.title, st)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = st.replace("_", " "),
                                                                fontSize = 10.sp,
                                                                color = if (isCur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Notes & Highlights
                            if (notes.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No notes or highlights created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(notes) { note ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(note.pageTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = { onDeleteNote(note) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                                if (note.selectedText.isNotBlank()) {
                                                    Text(
                                                        text = "Highlight: \"${note.selectedText}\"",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Text(note.noteText, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Saved Offline Articles
                            if (savedArticles.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No offline saved articles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(savedArticles) { article ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onOpenOfflineArticle(article)
                                                onDismiss()
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(article.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(article.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                }
                                                IconButton(onClick = { onDeleteOfflineArticle(article.url) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Bookmarks
                            if (bookmarks.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No bookmarks saved.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(bookmarks) { bm ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onSelectUrl(bm.url)
                                                onDismiss()
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(bm.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(bm.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                }
                                                IconButton(onClick = { onDeleteBookmark(bm.url) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // History
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (history.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = onClearHistory) {
                                            Text("Clear History", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                if (history.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No browsing history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(history) { h ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    onSelectUrl(h.url)
                                                    onDismiss()
                                                }
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(h.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(h.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
