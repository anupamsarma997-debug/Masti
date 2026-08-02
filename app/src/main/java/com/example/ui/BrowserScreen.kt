package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddressBar
import com.example.ui.components.AiResearchAssistantSheet
import com.example.ui.components.CitationDialog
import com.example.ui.components.CrossReferenceSheet
import com.example.ui.components.LibraryDialog
import com.example.ui.components.OpenAccessDialog
import com.example.ui.components.ReaderModeView
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TabSwitcherSheet
import com.example.ui.components.WebContainer

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabIndex by viewModel.activeTabIndex.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val allowAiProcessing by viewModel.allowAiProcessing.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val readingItems by viewModel.readingItems.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val savedArticles by viewModel.savedOfflineArticles.collectAsState()

    val aiResearchResult by viewModel.aiResearchResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val aiQuestionAnswer by viewModel.aiQuestionAnswer.collectAsState()
    val isAnsweringQuestion by viewModel.isAnsweringQuestion.collectAsState()

    val legalFreeResult by viewModel.legalFreeResult.collectAsState()
    val isLookupLoading by viewModel.isLookupLoading.collectAsState()
    val lookupError by viewModel.lookupError.collectAsState()

    val readerModeActive by viewModel.readerModeActive.collectAsState()
    val readerModeTitle by viewModel.readerModeTitle.collectAsState()
    val readerModeContent by viewModel.readerModeContent.collectAsState()

    val citationMetadata by viewModel.citationMetadata.collectAsState()
    val crossRefResult by viewModel.crossRefResult.collectAsState()

    val showTabSwitcher by viewModel.showTabSwitcher.collectAsState()
    val showAiSheet by viewModel.showAiSheet.collectAsState()
    val showOpenAccessDialog by viewModel.showOpenAccessDialog.collectAsState()
    val showLibraryDialog by viewModel.showLibraryDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showCitationDialog by viewModel.showCitationDialog.collectAsState()
    val showCrossRefSheet by viewModel.showCrossRefSheet.collectAsState()

    val activeTab = viewModel.activeTab
    val isCurrentBookmarked = bookmarks.any { it.url == activeTab?.url }

    if (readerModeActive) {
        ReaderModeView(
            title = readerModeTitle,
            content = readerModeContent,
            url = activeTab?.url ?: "",
            onClose = { viewModel.closeReaderMode() },
            onSaveOffline = { viewModel.saveCurrentArticleOffline() }
        )
        return
    }

    Scaffold(
        topBar = {
            AddressBar(
                activeTab = activeTab,
                tabCount = tabs.size,
                isBookmarked = isCurrentBookmarked,
                onNavigate = { viewModel.navigateActiveTab(it) },
                onHomeClick = { viewModel.updateActiveTabUrl("about:home") },
                onReloadClick = { viewModel.updateActiveTabUrl(activeTab?.url ?: "about:home") },
                onTabSwitcherClick = { viewModel.setShowTabSwitcher(true) },
                onOpenAccessClick = { viewModel.setShowOpenAccessDialog(true) },
                onToggleBookmark = { viewModel.toggleBookmarkCurrentTab() },
                onShowBookmarksHistory = { viewModel.setShowLibraryDialog(true) },
                onToggleDesktopMode = { viewModel.toggleDesktopUserAgent() },
                onToggleOverlayBlocker = { viewModel.toggleOverlayBlocker() },
                onShowSettings = { viewModel.setShowSettingsDialog(true) }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.updateActiveTabUrl("about:home") },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowLibraryDialog(true) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Library",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openReaderMode("") },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChromeReaderMode,
                            contentDescription = "Reader Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowOpenAccessDialog(true) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Find Legal Access",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowTabSwitcher(true) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tab,
                            contentDescription = "Tabs",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeTab != null && activeTab.url != "about:home") {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.triggerAiPageResearch() },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI") },
                    text = { Text("Ask about page", fontSize = 13.sp) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WebContainer(
                activeTab = activeTab,
                academicPortals = viewModel.academicPortals,
                onSearchSubmit = { viewModel.navigateActiveTab(it) },
                onVideoSearchSubmit = { viewModel.searchVideo(it) },
                onBookSearchSubmit = { viewModel.searchBook(it) },
                onPaperSearchSubmit = { viewModel.searchPaper(it) },
                onOpenDoiLookup = {
                    viewModel.setShowOpenAccessDialog(true)
                    viewModel.lookupPaperDoi(it)
                },
                onTitleChanged = { viewModel.updateActiveTabTitle(it) },
                onUrlChanged = { viewModel.updateActiveTabUrl(it) },
                onProgressChanged = { isLoading, progress ->
                    viewModel.updateActiveTabProgress(isLoading, progress)
                },
                onNavigationStateChanged = { canGoBack, canGoForward ->
                    viewModel.updateActiveTabNavigationState(canGoBack, canGoForward)
                },
                onDoiDetected = { doi ->
                    viewModel.setDetectedDoiForActiveTab(doi)
                }
            )
        }
    }

    // AI Research Bottom Sheet
    if (showAiSheet) {
        AiResearchAssistantSheet(
            url = activeTab?.url ?: "",
            pageTitle = activeTab?.title ?: "",
            aiResult = aiResearchResult,
            isLoading = isAiLoading,
            errorMessage = aiError,
            allowAiProcessing = allowAiProcessing,
            onDismiss = { viewModel.setShowAiSheet(false) },
            onRefreshAi = { viewModel.triggerAiPageResearch(forceRefresh = true) },
            onAskQuestion = { viewModel.askAiQuestion(it) },
            aiQuestionAnswer = aiQuestionAnswer,
            isAnsweringQuestion = isAnsweringQuestion
        )
    }

    // Tab Switcher Dialog
    if (showTabSwitcher) {
        TabSwitcherSheet(
            tabs = tabs,
            activeTabIndex = activeTabIndex,
            onSelectTab = { viewModel.selectTab(it) },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = { viewModel.addNewTab() },
            onDismiss = { viewModel.setShowTabSwitcher(false) }
        )
    }

    // Open Access Paper / Wayback / CrossRef Dialog
    if (showOpenAccessDialog) {
        OpenAccessDialog(
            initialDoiOrUrl = activeTab?.detectedDoi ?: if (activeTab?.url != "about:home") activeTab?.url else null,
            legalFreeResult = legalFreeResult,
            isLoading = isLookupLoading,
            errorMessage = lookupError,
            onLookupDoi = { viewModel.lookupPaperDoi(it) },
            onLookupWayback = { viewModel.lookupWaybackArchive(it) },
            onOpenUrlInNewTab = { url ->
                viewModel.addNewTab(url)
                viewModel.setShowOpenAccessDialog(false)
            },
            onExportCitation = { meta ->
                viewModel.openCitationDialog(meta)
            },
            onViewCrossReferences = { res ->
                viewModel.openCrossRefSheet(res)
            },
            onDismiss = { viewModel.setShowOpenAccessDialog(false) }
        )
    }

    // Library Dialog
    if (showLibraryDialog) {
        LibraryDialog(
            bookmarks = bookmarks,
            history = history,
            readingItems = readingItems,
            notes = notes,
            savedArticles = savedArticles,
            onSelectUrl = { url ->
                viewModel.navigateActiveTab(url)
                viewModel.setShowLibraryDialog(false)
            },
            onDeleteBookmark = { url -> viewModel.removeBookmark(url) },
            onClearHistory = { viewModel.clearAllHistory() },
            onUpdateReadingStatus = { url, title, status ->
                viewModel.updateReadingStatus(url, title, status)
            },
            onDeleteNote = { note -> viewModel.deleteNote(note) },
            onOpenOfflineArticle = { article ->
                viewModel.openReaderMode(article.extractedContent)
                viewModel.setShowLibraryDialog(false)
            },
            onDeleteOfflineArticle = { url -> viewModel.deleteOfflineArticle(url) },
            onDismiss = { viewModel.setShowLibraryDialog(false) }
        )
    }

    // Citation Dialog
    if (showCitationDialog && citationMetadata != null) {
        CitationDialog(
            metadata = citationMetadata!!,
            onDismiss = { viewModel.closeCitationDialog() }
        )
    }

    // Cross Reference Sheet
    if (showCrossRefSheet && crossRefResult != null) {
        CrossReferenceSheet(
            doi = crossRefResult!!.doi,
            paperTitle = crossRefResult!!.title,
            citers = crossRefResult!!.citers,
            references = crossRefResult!!.references,
            onDismiss = { viewModel.closeCrossRefSheet() },
            onSelectPaper = { query ->
                viewModel.searchPaper(query)
                viewModel.closeCrossRefSheet()
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            allowAiProcessing = allowAiProcessing,
            onToggleAiProcessing = { viewModel.toggleAiProcessing() },
            onUpdateSearchEngine = { viewModel.updateSearchEngine(it) },
            onToggleDesktopMode = { viewModel.toggleDesktopUserAgent() },
            onToggleOverlayBlocker = { viewModel.toggleOverlayBlocker() },
            onUpdateProxySettings = { host, port, enabled ->
                viewModel.updateProxySettings(host, port, enabled)
            },
            onDismiss = { viewModel.setShowSettingsDialog(false) }
        )
    }
}

