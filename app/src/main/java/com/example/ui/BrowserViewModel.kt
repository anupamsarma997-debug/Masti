package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ArticleCacheEntity
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.data.NoteEntity
import com.example.data.ReadingItemEntity
import com.example.model.AcademicPortal
import com.example.model.BrowserSettings
import com.example.model.SearchEngine
import com.example.model.WebTab
import com.example.util.AiResearchResult
import com.example.util.GeminiRepository
import com.example.util.IntentRouter
import com.example.util.LegalFreeAccessResult
import com.example.util.LibraryRepository
import com.example.util.OpenAccessRepository
import com.example.util.PaperMetadata
import com.example.util.SearchIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val openAccessRepo = OpenAccessRepository()
    private val geminiRepo = GeminiRepository(aiCacheDao = db.aiCacheDao())
    private val libraryRepo = LibraryRepository(
        bookmarkDao = db.bookmarkDao(),
        historyDao = db.historyDao(),
        readingItemDao = db.readingItemDao(),
        noteDao = db.noteDao(),
        articleCacheDao = db.articleCacheDao()
    )

    // Room Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = libraryRepo.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = libraryRepo.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingItems: StateFlow<List<ReadingItemEntity>> = libraryRepo.allReadingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = libraryRepo.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedOfflineArticles: StateFlow<List<ArticleCacheEntity>> = libraryRepo.allSavedArticles
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

    private val _allowAiProcessing = MutableStateFlow(true)
    val allowAiProcessing: StateFlow<Boolean> = _allowAiProcessing.asStateFlow()

    // AI Research Assistant State
    private val _aiResearchResult = MutableStateFlow<AiResearchResult?>(null)
    val aiResearchResult: StateFlow<AiResearchResult?> = _aiResearchResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _aiQuestionAnswer = MutableStateFlow<String?>(null)
    val aiQuestionAnswer: StateFlow<String?> = _aiQuestionAnswer.asStateFlow()

    private val _isAnsweringQuestion = MutableStateFlow(false)
    val isAnsweringQuestion: StateFlow<Boolean> = _isAnsweringQuestion.asStateFlow()

    // Legal Free Access State
    private val _legalFreeResult = MutableStateFlow<LegalFreeAccessResult?>(null)
    val legalFreeResult: StateFlow<LegalFreeAccessResult?> = _legalFreeResult.asStateFlow()

    private val _isLookupLoading = MutableStateFlow(false)
    val isLookupLoading: StateFlow<Boolean> = _isLookupLoading.asStateFlow()

    private val _lookupError = MutableStateFlow<String?>(null)
    val lookupError: StateFlow<String?> = _lookupError.asStateFlow()

    // Reader Mode State
    private val _readerModeActive = MutableStateFlow(false)
    val readerModeActive: StateFlow<Boolean> = _readerModeActive.asStateFlow()

    private val _readerModeTitle = MutableStateFlow("")
    val readerModeTitle: StateFlow<String> = _readerModeTitle.asStateFlow()

    private val _readerModeContent = MutableStateFlow("")
    val readerModeContent: StateFlow<String> = _readerModeContent.asStateFlow()

    // Citation & CrossRef Dialog States
    private val _citationMetadata = MutableStateFlow<PaperMetadata?>(null)
    val citationMetadata: StateFlow<PaperMetadata?> = _citationMetadata.asStateFlow()

    private val _crossRefResult = MutableStateFlow<LegalFreeAccessResult?>(null)
    val crossRefResult: StateFlow<LegalFreeAccessResult?> = _crossRefResult.asStateFlow()

    // Dialog Visibilities
    private val _showAiSheet = MutableStateFlow(false)
    val showAiSheet: StateFlow<Boolean> = _showAiSheet.asStateFlow()

    private val _showOpenAccessDialog = MutableStateFlow(false)
    val showOpenAccessDialog: StateFlow<Boolean> = _showOpenAccessDialog.asStateFlow()

    private val _showLibraryDialog = MutableStateFlow(false)
    val showLibraryDialog: StateFlow<Boolean> = _showLibraryDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showTabSwitcher = MutableStateFlow(false)
    val showTabSwitcher: StateFlow<Boolean> = _showTabSwitcher.asStateFlow()

    private val _showCitationDialog = MutableStateFlow(false)
    val showCitationDialog: StateFlow<Boolean> = _showCitationDialog.asStateFlow()

    private val _showCrossRefSheet = MutableStateFlow(false)
    val showCrossRefSheet: StateFlow<Boolean> = _showCrossRefSheet.asStateFlow()

    // Page text extracted for AI
    var currentExtractedPageText: String = ""

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
        when (val routed = IntentRouter.routeInput(input)) {
            is SearchIntent.DoiLookup -> {
                lookupPaperDoi(routed.doi)
                _showOpenAccessDialog.value = true
            }
            is SearchIntent.DirectUrl -> {
                updateActiveTabUrl(routed.url)
            }
            is SearchIntent.TopicSearch -> {
                if (routed.query.isNotBlank()) {
                    val searchUrl = _settings.value.searchEngine.searchUrl + java.net.URLEncoder.encode(routed.query, "UTF-8")
                    updateActiveTabUrl(searchUrl)
                }
            }
        }
    }

    fun updateActiveTabUrl(url: String) {
        val index = _activeTabIndex.value
        val list = _tabs.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(url = url)
            _tabs.value = list

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

    fun toggleAiProcessing() {
        _allowAiProcessing.value = !_allowAiProcessing.value
    }

    // AI Research Assistant Actions
    fun triggerAiPageResearch(forceRefresh: Boolean = false) {
        val current = activeTab ?: return
        if (current.url.isBlank() || current.url == "about:home") return

        viewModelScope.launch {
            _isAiLoading.value = true
            _aiError.value = null
            _aiQuestionAnswer.value = null
            _showAiSheet.value = true

            try {
                val res = geminiRepo.getPageAiResearch(
                    url = current.url,
                    pageTitle = current.title,
                    extractedText = currentExtractedPageText,
                    forceRefresh = forceRefresh
                )
                _aiResearchResult.value = res
            } catch (e: Exception) {
                _aiError.value = e.localizedMessage ?: "Failed to generate AI analysis."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun askAiQuestion(question: String) {
        val current = activeTab ?: return
        viewModelScope.launch {
            _isAnsweringQuestion.value = true
            try {
                val answer = geminiRepo.askFollowUpQuestion(
                    pageTitle = current.title,
                    extractedText = currentExtractedPageText,
                    question = question
                )
                _aiQuestionAnswer.value = answer
            } catch (e: Exception) {
                _aiQuestionAnswer.value = "Error: ${e.localizedMessage}"
            } finally {
                _isAnsweringQuestion.value = false
            }
        }
    }

    // Legal Free Access & Wayback Lookup
    fun lookupPaperDoi(doiInput: String) {
        val current = activeTab
        viewModelScope.launch {
            _isLookupLoading.value = true
            _lookupError.value = null
            _legalFreeResult.value = null

            val result = openAccessRepo.findLegalFreeAccess(doiInput, pageUrl = current?.url)
            _legalFreeResult.value = result
            if (result.errorMessage != null && !result.isOaAvailable) {
                _lookupError.value = result.errorMessage
            }
            _isLookupLoading.value = false
        }
    }

    fun lookupWaybackArchive(targetUrl: String) {
        viewModelScope.launch {
            _isLookupLoading.value = true
            _lookupError.value = null

            val result = openAccessRepo.findLegalFreeAccess(doi = "", pageUrl = targetUrl)
            _legalFreeResult.value = result
            _isLookupLoading.value = false
        }
    }

    // Reader Mode Actions
    fun openReaderMode(extractedText: String) {
        val current = activeTab ?: return
        _readerModeTitle.value = current.title
        _readerModeContent.value = extractedText.ifBlank { currentExtractedPageText }
        _readerModeActive.value = true
    }

    fun closeReaderMode() {
        _readerModeActive.value = false
    }

    fun saveCurrentArticleOffline() {
        val current = activeTab ?: return
        viewModelScope.launch {
            libraryRepo.saveArticleOffline(
                url = current.url,
                title = current.title,
                content = currentExtractedPageText
            )
        }
    }

    // Citations & Cross References
    fun openCitationDialog(metadata: PaperMetadata) {
        _citationMetadata.value = metadata
        _showCitationDialog.value = true
    }

    fun closeCitationDialog() {
        _showCitationDialog.value = false
    }

    fun openCrossRefSheet(result: LegalFreeAccessResult) {
        _crossRefResult.value = result
        _showCrossRefSheet.value = true
    }

    fun closeCrossRefSheet() {
        _showCrossRefSheet.value = false
    }

    // Library & Notes Actions
    fun toggleBookmarkCurrentTab() {
        val current = activeTab ?: return
        if (current.url.isBlank() || current.url == "about:home") return

        viewModelScope.launch {
            libraryRepo.addBookmark(
                title = current.title.ifBlank { current.url },
                url = current.url
            )
        }
    }

    fun removeBookmark(url: String) {
        viewModelScope.launch {
            libraryRepo.removeBookmark(url)
        }
    }

    private fun recordHistory(title: String, url: String) {
        viewModelScope.launch {
            libraryRepo.addHistory(
                title = title.ifBlank { url },
                url = url
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            libraryRepo.clearHistory()
        }
    }

    fun updateReadingStatus(url: String, title: String, status: String) {
        viewModelScope.launch {
            libraryRepo.updateReadingStatus(url, title, status)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            libraryRepo.deleteNote(note)
        }
    }

    fun deleteOfflineArticle(url: String) {
        viewModelScope.launch {
            libraryRepo.deleteOfflineArticle(url)
        }
    }

    fun updateSearchEngine(engine: SearchEngine) {
        _settings.update { it.copy(searchEngine = engine) }
    }

    fun updateProxySettings(host: String, port: Int, enabled: Boolean) {
        _settings.update { it.copy(proxyHost = host, proxyPort = port, isProxyEnabled = enabled) }
    }

    // Dialog Toggle Functions
    fun setShowAiSheet(show: Boolean) {
        _showAiSheet.value = show
    }

    fun setShowOpenAccessDialog(show: Boolean) {
        _showOpenAccessDialog.value = show
    }

    fun setShowLibraryDialog(show: Boolean) {
        _showLibraryDialog.value = show
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowTabSwitcher(show: Boolean) {
        _showTabSwitcher.value = show
    }
}

