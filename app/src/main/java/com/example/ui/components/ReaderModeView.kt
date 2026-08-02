package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ReaderTheme(val bg: Color, val text: Color) {
    LIGHT(Color(0xFFFFFFFF), Color(0xFF1C1B1F)),
    SEPIA(Color(0xFFFBF0D9), Color(0xFF5F4B32)),
    DARK(Color(0xFF121212), Color(0xFFE6E1E5))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderModeView(
    title: String,
    content: String,
    url: String,
    onClose: () -> Unit,
    onSaveOffline: () -> Unit
) {
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var selectedTheme by remember { mutableStateOf(ReaderTheme.SEPIA) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Distraction-Free Reader", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Font Controls")
                    }
                    IconButton(onClick = onSaveOffline) {
                        Icon(Icons.Default.Save, contentDescription = "Save Offline")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = selectedTheme.bg,
                    titleContentColor = selectedTheme.text,
                    navigationIconContentColor = selectedTheme.text,
                    actionIconContentColor = selectedTheme.text
                )
            )
        },
        containerColor = selectedTheme.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showSettings) {
                Surface(
                    color = selectedTheme.bg,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Font Size: ${fontSizeSp.toInt()} sp", color = selectedTheme.text)
                            Slider(
                                value = fontSizeSp,
                                onValueChange = { fontSizeSp = it },
                                valueRange = 12f..28f,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Theme:", color = selectedTheme.text, fontSize = 12.sp)
                            ReaderTheme.values().forEach { theme ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(theme.bg)
                                        .clickable { selectedTheme = theme },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedTheme == theme) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(theme.text)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = title.ifBlank { "Untitled Document" },
                    fontSize = (fontSizeSp + 6).sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedTheme.text,
                    lineHeight = (fontSizeSp + 10).sp,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = url,
                    fontSize = 11.sp,
                    color = selectedTheme.text.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = selectedTheme.text.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (content.isNotBlank()) content else "No readable article text extracted from this page.",
                    fontSize = fontSizeSp.sp,
                    color = selectedTheme.text,
                    lineHeight = (fontSizeSp * 1.5f).sp,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
