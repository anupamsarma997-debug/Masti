package com.example.model

import java.util.UUID

enum class SearchEngine(val displayName: String, val searchUrl: String) {
    DUCKDUCKGO("DuckDuckGo", "https://html.duckduckgo.com/html/?q="),
    GOOGLE("Google", "https://www.google.com/search?q="),
    ARXIV("arXiv Academic", "https://arxiv.org/search/?searchtype=all&query="),
    WIKIPEDIA("Wikipedia", "https://en.wikipedia.org/w/index.php?search="),
    STARTPAGE("Startpage", "https://www.startpage.com/sp/search?query=")
}

data class WebTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "OpenEdu Search",
    val url: String = "https://html.duckduckgo.com",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val detectedDoi: String? = null,
    val isDesktopAgent: Boolean = false,
    val isOverlayBlockerActive: Boolean = true
)

data class BrowserSettings(
    val isDesktopUserAgent: Boolean = false,
    val isOverlayBlockerEnabled: Boolean = true,
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val proxyHost: String = "127.0.0.1",
    val proxyPort: Int = 9050,
    val isProxyEnabled: Boolean = false
)

data class AcademicPortal(
    val name: String,
    val description: String,
    val url: String,
    val category: String
)
