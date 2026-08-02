package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddressBar
import com.example.ui.components.BookmarksHistoryDialog
import com.example.ui.components.OpenAccessDialog
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
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()

    val unpaywallResult by viewModel.unpaywallResult.collectAsState()
    val waybackResult by viewModel.waybackResult.collectAsState()
    val isLookupLoading by viewModel.isLookupLoading.collectAsState()
    val lookupError by viewModel.lookupError.collectAsState()

    val showTabSwitcher by viewModel.showTabSwitcher.collectAsState()
    val showOpenAccessDialog by viewModel.showOpenAccessDialog.collectAsState()
    val showBookmarksHistoryDialog by viewModel.showBookmarksHistoryDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()

    val activeTab = viewModel.activeTab
    val isCurrentBookmarked = bookmarks.any { it.url == activeTab?.url }

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
                onShowBookmarksHistory = { viewModel.setShowBookmarksHistoryDialog(true) },
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
                        onClick = { viewModel.setShowOpenAccessDialog(true) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Unpaywall Paper Search",
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

    // Open Access Paper / Wayback Dialog
    if (showOpenAccessDialog) {
        OpenAccessDialog(
            initialDoiOrUrl = activeTab?.detectedDoi ?: if (activeTab?.url != "about:home") activeTab?.url else null,
            unpaywallResult = unpaywallResult,
            waybackResult = waybackResult,
            isLoading = isLookupLoading,
            errorMessage = lookupError,
            onLookupDoi = { viewModel.lookupPaperDoi(it) },
            onLookupWayback = { viewModel.lookupWaybackArchive(it) },
            onOpenUrlInNewTab = { url ->
                viewModel.addNewTab(url)
                viewModel.setShowOpenAccessDialog(false)
            },
            onDismiss = { viewModel.setShowOpenAccessDialog(false) }
        )
    }

    // Bookmarks & History Dialog
    if (showBookmarksHistoryDialog) {
        BookmarksHistoryDialog(
            bookmarks = bookmarks,
            history = history,
            onOpenUrl = { url ->
                viewModel.navigateActiveTab(url)
                viewModel.setShowBookmarksHistoryDialog(false)
            },
            onDeleteBookmark = { viewModel.deleteBookmark(it) },
            onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
            onClearAllHistory = { viewModel.clearAllHistory() },
            onDismiss = { viewModel.setShowBookmarksHistoryDialog(false) }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
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
