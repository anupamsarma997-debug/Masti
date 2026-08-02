package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.AcademicPortal
import com.example.model.WebTab
import com.example.script.UserScripts

private const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebContainer(
    activeTab: WebTab?,
    academicPortals: List<AcademicPortal>,
    onSearchSubmit: (String) -> Unit,
    onOpenDoiLookup: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onProgressChanged: (Boolean, Int) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onDoiDetected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeTab?.url == "about:home") {
        HomeScreenView(
            academicPortals = academicPortals,
            onSearchSubmit = onSearchSubmit,
            onOpenDoiLookup = onOpenDoiLookup,
            onPortalClick = onSearchSubmit,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current

    val webView = remember(activeTab?.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowContentAccess = true
                allowFileAccess = false
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgressChanged(newProgress < 100, newProgress)
                    onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        onTitleChanged(title)
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    onProgressChanged(true, 10)
                    if (!url.isNullOrBlank()) {
                        onUrlChanged(url)
                    }
                    onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onProgressChanged(false, 100)
                    if (!url.isNullOrBlank()) {
                        onUrlChanged(url)
                    }
                    onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)

                    // Inject Overlay Blocker script if enabled
                    if (activeTab?.isOverlayBlockerActive == true) {
                        view?.evaluateJavascript(UserScripts.OVERLAY_BLOCKER_JS, null)
                    }

                    // Extract DOI from page if present
                    view?.evaluateJavascript(UserScripts.EXTRACT_DOI_JS) { value ->
                        val cleanedValue = value?.replace("\"", "")?.trim()
                        if (!cleanedValue.isNullOrBlank() && cleanedValue != "null") {
                            onDoiDetected(cleanedValue)
                        } else {
                            onDoiDetected(null)
                        }
                    }
                }
            }
        }
    }

    // Handle User-Agent updates
    LaunchedEffect(activeTab?.isDesktopAgent) {
        if (activeTab?.isDesktopAgent == true) {
            webView.settings.userAgentString = DESKTOP_UA
            webView.settings.useWideViewPort = true
        } else {
            webView.settings.userAgentString = null // Default mobile UA
            webView.settings.useWideViewPort = true
        }
        if (webView.url != activeTab?.url && activeTab?.url?.isNotBlank() == true) {
            webView.reload()
        }
    }

    // Handle URL loads
    LaunchedEffect(activeTab?.url) {
        val currentUrl = webView.url
        val targetUrl = activeTab?.url
        if (!targetUrl.isNullOrBlank() && targetUrl != "about:home" && targetUrl != currentUrl) {
            webView.loadUrl(targetUrl)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(activeTab?.id) {
        onDispose {
            webView.stopLoading()
        }
    }
}
