package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.model.AcademicPortal
import com.example.model.BrowserSettings
import com.example.model.SearchEngine
import com.example.model.WebTab
import com.example.network.OpenAccessService
import com.example.network.UnpaywallResponse
import com.example.network.WaybackResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.historyDao()
    private val openAccessService = OpenAccessService()

    // Room Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = historyDao.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs State
    private val _tabs = MutableStateFlow<List<WebTab>>(
        listOf(WebTab(title = "OpenEdu Home", url = "about:home"))
    )
    val tabs: StateFlow<List<WebTab>> = _tabs.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    val activeTab: WebTab?
        get() = _tabs.value.getOrNull(_activeTabIndex.value)

    // Settings State
    private val _settings = MutableStateFlow(BrowserSettings())
    val settings: StateFlow<BrowserSettings> = _settings.asStateFlow()

    // Open Access Lookup State
    private val _unpaywallResult = MutableStateFlow<UnpaywallResponse?>(null)
    val unpaywallResult: StateFlow<UnpaywallResponse?> = _unpaywallResult.asStateFlow()

    private val _waybackResult = MutableStateFlow<WaybackResponse?>(null)
    val waybackResult: StateFlow<WaybackResponse?> = _waybackResult.asStateFlow()

    private val _isLookupLoading = MutableStateFlow(false)
    val isLookupLoading: StateFlow<Boolean> = _isLookupLoading.asStateFlow()

    private val _lookupError = MutableStateFlow<String?>(null)
    val lookupError: StateFlow<String?> = _lookupError.asStateFlow()

    // Dialog visibilities
    private val _showOpenAccessDialog = MutableStateFlow(false)
    val showOpenAccessDialog: StateFlow<Boolean> = _showOpenAccessDialog.asStateFlow()

    private val _showBookmarksHistoryDialog = MutableStateFlow(false)
    val showBookmarksHistoryDialog: StateFlow<Boolean> = _showBookmarksHistoryDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showTabSwitcher = MutableStateFlow(false)
    val showTabSwitcher: StateFlow<Boolean> = _showTabSwitcher.asStateFlow()

    // Academic Portals / Quick Dials
    val academicPortals = listOf(
        AcademicPortal("Open Library", "Millions of free digital books & borrowing", "https://openlibrary.org", "Books"),
        AcademicPortal("YouTube Education", "Video lectures, tutorials, and educational media", "https://www.youtube.com", "Videos"),
        AcademicPortal("Google Books", "Search the world's most comprehensive index of full-text books", "https://books.google.com", "Books"),
        AcademicPortal("MIT OpenCourseWare", "Free lecture notes, exams, and videos from MIT", "https://ocw.mit.edu", "Videos"),
        AcademicPortal("Project Gutenberg", "Over 70,000 free ebooks to download or read online", "https://www.gutenberg.org", "Books"),
        AcademicPortal("Khan Academy", "Free online courses, lessons & practice videos", "https://www.khanacademy.org", "Videos"),
        AcademicPortal("arXiv.org", "Open-access archive for 2M+ scholarly articles", "https://arxiv.org", "Papers"),
        AcademicPortal("DOAJ", "Directory of Open Access Journals", "https://doaj.org", "Journals"),
        AcademicPortal("PubMed Central", "Free full-text archive of biomedical literature", "https://www.ncbi.nlm.nih.gov/pmc/", "Medical"),
        AcademicPortal("Internet Archive", "Millions of free books, movies, & web pages", "https://archive.org", "Archive"),
        AcademicPortal("Wikipedia", "The Free Encyclopedia", "https://en.wikipedia.org", "Reference"),
        AcademicPortal("Unpaywall", "Harvests Open Access content from 50K+ publishers", "https://unpaywall.org", "Open Access")
    )

    // Tab Operations
    fun addNewTab(url: String = "about:home") {
        val newTab = WebTab(
            title = if (url == "about:home") "OpenEdu Home" else "Loading...",
            url = url,
            isDesktopAgent = _settings.value.isDesktopUserAgent,
            isOverlayBlockerActive = _settings.value.isOverlayBlockerEnabled
        )
        _tabs.update { it + newTab }
        _activeTabIndex.value = _tabs.value.size - 1
        _showTabSwitcher.value = false
    }

    fun closeTab(index: Int) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // Re-initialize single home tab if all closed
            _tabs.value = listOf(WebTab(title = "OpenEdu Home", url = "about:home"))
            _activeTabIndex.value = 0
            return
        }

        val updatedTabs = currentTabs.filterIndexed { i, _ -> i != index }
        _tabs.value = updatedTabs

        var nextIndex = _activeTabIndex.value
        if (index <= nextIndex && nextIndex > 0) {
            nextIndex -= 1
        }
        _activeTabIndex.value = nextIndex.coerceIn(0, updatedTabs.size - 1)
    }

    fun selectTab(index: Int) {
        if (index in 0 until _tabs.value.size) {
            _activeTabIndex.value = index
            _showTabSwitcher.value = false
        }
    }

    fun searchVideo(query: String) {
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        navigateActiveTab("https://www.youtube.com/results?search_query=$encoded")
    }

    fun searchBook(query: String) {
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        navigateActiveTab("https://openlibrary.org/search?q=$encoded")
    }

    fun searchPaper(query: String) {
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        navigateActiveTab("https://arxiv.org/search/?searchtype=all&query=$encoded")
    }

    fun navigateActiveTab(input: String) {
        val targetUrl = processUrlInput(input)
        updateActiveTabUrl(targetUrl)
    }

    private fun processUrlInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed == "about:home") return "about:home"

        // Check if input is a DOI (e.g., 10.1038/nature12345 or doi:10...)
        if (trimmed.startsWith("10.") || trimmed.startsWith("doi:", ignoreCase = true) || trimmed.contains("doi.org/10.")) {
            val doi = trimmed.removePrefix("https://").removePrefix("http://").removePrefix("doi.org/").removePrefix("doi:")
            return "https://doi.org/$doi"
        }

        // Check if full URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // Check if domain-like without scheme (e.g. arxiv.org, wikipedia.org)
        if (trimmed.contains(".") && !trimmed.contains(" ")) {
            return "https://$trimmed"
        }

        // Default to configured search engine
        return _settings.value.searchEngine.searchUrl + java.net.URLEncoder.encode(trimmed, "UTF-8")
    }

    fun updateActiveTabUrl(url: String) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(url = url)
            _tabs.value = list

            // Record to history if valid web page
            if (url != "about:home" && !url.startsWith("data:")) {
                recordHistory(list[index].title, url)
            }
        }
    }

    fun updateActiveTabTitle(title: String) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            val updatedTitle = title.ifBlank { "Untitled Page" }
            list[index] = list[index].copy(title = updatedTitle)
            _tabs.value = list
        }
    }

    fun updateActiveTabProgress(isLoading: Boolean, progress: Int) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(isLoading = isLoading, progress = progress)
            _tabs.value = list
        }
    }

    fun updateActiveTabNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(canGoBack = canGoBack, canGoForward = canGoForward)
            _tabs.value = list
        }
    }

    fun setDetectedDoiForActiveTab(doi: String?) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(detectedDoi = doi)
            _tabs.value = list
        }
    }

    fun toggleDesktopUserAgent() {
        val newDesktopState = !_settings.value.isDesktopUserAgent
        _settings.update { it.copy(isDesktopUserAgent = newDesktopState) }

        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(isDesktopAgent = newDesktopState)
            _tabs.value = list
        }
    }

    fun toggleOverlayBlocker() {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            val newState = !list[index].isOverlayBlockerActive
            list[index] = list[index].copy(isOverlayBlockerActive = newState)
            _tabs.value = list
        }
    }

    // Room DB Actions
    fun toggleBookmarkCurrentTab() {
        val current = activeTab ?: return
        if (current.url.isBlank() || current.url == "about:home") return

        viewModelScope.launch {
            val isBookmarked = bookmarkDao.isBookmarked(current.url)
            // Save bookmark
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    title = current.title.ifBlank { current.url },
                    url = current.url
                )
            )
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(id)
        }
    }

    private fun recordHistory(title: String, url: String) {
        viewModelScope.launch {
            historyDao.insertHistory(
                HistoryEntity(
                    title = title.ifBlank { url },
                    url = url
                )
            )
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            historyDao.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
        }
    }

    // Open Access & Wayback Machine Lookups
    fun lookupPaperDoi(doiInput: String) {
        viewModelScope.launch {
            _isLookupLoading.value = true
            _lookupError.value = null
            _unpaywallResult.value = null

            val response = openAccessService.checkUnpaywall(doiInput)
            if (response != null) {
                _unpaywallResult.value = response
            } else {
                _lookupError.value = "No Open Access version found or invalid DOI format."
            }
            _isLookupLoading.value = false
        }
    }

    fun lookupWaybackArchive(targetUrl: String) {
        viewModelScope.launch {
            _isLookupLoading.value = true
            _lookupError.value = null
            _waybackResult.value = null

            val response = openAccessService.checkWaybackMachine(targetUrl)
            if (response != null && response.archivedSnapshots?.closest?.available == true) {
                _waybackResult.value = response
            } else {
                _lookupError.value = "No archived snapshot available in Wayback Machine for this URL."
            }
            _isLookupLoading.value = false
        }
    }

    fun updateSearchEngine(engine: SearchEngine) {
        _settings.update { it.copy(searchEngine = engine) }
    }

    fun updateProxySettings(host: String, port: Int, enabled: Boolean) {
        _settings.update { it.copy(proxyHost = host, proxyPort = port, isProxyEnabled = enabled) }
    }

    // Dialog Toggle Functions
    fun setShowOpenAccessDialog(show: Boolean) {
        _showOpenAccessDialog.value = show
    }

    fun setShowBookmarksHistoryDialog(show: Boolean) {
        _showBookmarksHistoryDialog.value = show
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowTabSwitcher(show: Boolean) {
        _showTabSwitcher.value = show
    }
}
