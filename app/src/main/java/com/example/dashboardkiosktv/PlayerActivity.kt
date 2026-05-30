package com.example.dashboardkiosktv

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.DashboardPage
import com.example.dashboardkiosktv.data.PlaylistParser
import com.example.dashboardkiosktv.data.PlaylistStorage

class PlayerActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val handler = Handler(Looper.getMainLooper())

    private var dashboardPages: List<DashboardPage> = emptyList()
    private var currentIndex = 0
    private var loopPlaylist = true
    private var isPlayerActive = false

    private val rotateRunnable = object : Runnable {
        override fun run() {
            showNextDashboard()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_player)

        webView = findViewById(R.id.dashboardWebView)

        configureWebView()
        loadSavedPlaylist()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            loadWithOverviewMode = true
            useWideViewPort = true

            builtInZoomControls = false
            displayZoomControls = false

            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                if (isPlayerActive) {
                    scheduleNextDashboard()
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)

                if (isPlayerActive) {
                    scheduleNextDashboard()
                }
            }
        }
    }

    private fun loadSavedPlaylist() {
        val storage = PlaylistStorage(this)
        val playlistText = storage.getPlaylistText()

        if (playlistText.isNullOrBlank()) {
            openPlaylistEditor()
            return
        }

        val parsedPages = PlaylistParser.parse(playlistText)

        if (parsedPages.isEmpty()) {
            openPlaylistEditor()
            return
        }

        dashboardPages = parsedPages
        loopPlaylist = storage.isLoopEnabled()
        currentIndex = 0
        isPlayerActive = true

        loadCurrentDashboard()
    }

    private fun loadCurrentDashboard() {
        if (dashboardPages.isEmpty()) {
            openPlaylistEditor()
            return
        }

        handler.removeCallbacks(rotateRunnable)

        val page = dashboardPages[currentIndex]
        webView.loadUrl(page.url)
    }

    private fun scheduleNextDashboard() {
        if (dashboardPages.isEmpty()) return

        handler.removeCallbacks(rotateRunnable)

        val page = dashboardPages[currentIndex]

        handler.postDelayed(
            rotateRunnable,
            page.displaySeconds * 1000L
        )
    }

    private fun showNextDashboard() {
        if (dashboardPages.isEmpty()) {
            openPlaylistEditor()
            return
        }

        val isLastPage = currentIndex == dashboardPages.lastIndex

        if (isLastPage && !loopPlaylist) {
            handler.removeCallbacks(rotateRunnable)
            return
        }

        currentIndex = if (isLastPage) {
            0
        } else {
            currentIndex + 1
        }

        loadCurrentDashboard()
    }

    private fun openPlaylistEditor() {
        isPlayerActive = false
        handler.removeCallbacks(rotateRunnable)

        startActivity(Intent(this, PlaylistActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Temporary actual-app step:
        // Next step will replace this with PIN dialog.
        openPlaylistEditor()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            openPlaylistEditor()
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }
}