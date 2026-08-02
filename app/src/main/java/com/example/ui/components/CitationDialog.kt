package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CitationGenerator
import com.example.util.PaperMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitationDialog(
    metadata: PaperMetadata,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val apa = remember(metadata) { CitationGenerator.generateApa(metadata) }
    val mla = remember(metadata) { CitationGenerator.generateMla(metadata) }
    val bibtex = remember(metadata) { CitationGenerator.generateBibTex(metadata) }

    val activeCitationText = when (selectedTab) {
        0 -> apa
        1 -> mla
        else -> bibtex
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Academic Citation", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("APA") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("MLA") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("BibTeX") })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "APA Format"
                                    1 -> "MLA Format"
                                    else -> "BibTeX Format"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    copyToClipboard(context, activeCitationText)
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activeCitationText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = if (selectedTab == 2) FontFamily.Monospace else FontFamily.Default
                            ),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    copyToClipboard(context, activeCitationText)
                    onDismiss()
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy & Close")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Academic Citation", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Citation copied to clipboard!", Toast.LENGTH_SHORT).show()
}
