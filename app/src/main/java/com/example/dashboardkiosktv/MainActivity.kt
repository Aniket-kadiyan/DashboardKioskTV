package com.example.dashboardkiosktv

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class DashboardPage(
    val title: String,
    val url: String,
    val displaySeconds: Long
)

class MainActivity : AppCompatActivity() {

    private lateinit var adminPanel: LinearLayout
    private lateinit var playlistInput: EditText
    private lateinit var loopCheckbox: CheckBox
    private lateinit var startButton: Button
    private lateinit var webView: WebView

    private val handler = Handler(Looper.getMainLooper())

    private var dashboardPages: List<DashboardPage> = emptyList()
    private var currentIndex = 0
    private var loopPlaylist = true
    private var isPlaying = false

    private val prefs by lazy {
        getSharedPreferences("dashboard_kiosk_settings", Context.MODE_PRIVATE)
    }

    private val rotateRunnable = object : Runnable {
        override fun run() {
            showNextDashboard()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep emulator / TV awake while app is open.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Full-screen app.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        adminPanel = findViewById(R.id.adminPanel)
        playlistInput = findViewById(R.id.playlistInput)
        loopCheckbox = findViewById(R.id.loopCheckbox)
        startButton = findViewById(R.id.startButton)
        webView = findViewById(R.id.dashboardWebView)

        configureWebView()
        loadSavedPlaylistIntoEditor()

        startButton.setOnClickListener {
            savePlaylistAndStart()
        }

        // If a playlist was already saved earlier, start it automatically.
        val savedText = prefs.getString(KEY_PLAYLIST_TEXT, null)
        if (!savedText.isNullOrBlank()) {
            startPlaylistFromText(savedText, prefs.getBoolean(KEY_LOOP_PLAYLIST, true))
        }
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

                if (isPlaying) {
                    scheduleNextDashboard()
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)

                if (isPlaying) {
                    scheduleNextDashboard()
                }
            }
        }
    }

    private fun loadSavedPlaylistIntoEditor() {
        val defaultPlaylistText = """
            Example | https://example.com | 10
            Wikipedia | https://www.wikipedia.org | 20
        """.trimIndent()

        val savedText = prefs.getString(KEY_PLAYLIST_TEXT, defaultPlaylistText)
        val savedLoop = prefs.getBoolean(KEY_LOOP_PLAYLIST, true)

        playlistInput.setText(savedText)
        loopCheckbox.isChecked = savedLoop
    }

    private fun savePlaylistAndStart() {
        val playlistText = playlistInput.text.toString()
        val loop = loopCheckbox.isChecked

        val parsedPages = parsePlaylistText(playlistText)

        if (parsedPages.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter at least one valid dashboard line.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        prefs.edit()
            .putString(KEY_PLAYLIST_TEXT, playlistText)
            .putBoolean(KEY_LOOP_PLAYLIST, loop)
            .apply()

        startPlaylist(parsedPages, loop)
    }

    private fun startPlaylistFromText(playlistText: String, loop: Boolean) {
        val parsedPages = parsePlaylistText(playlistText)

        if (parsedPages.isEmpty()) {
            showAdminPanel()
            return
        }

        startPlaylist(parsedPages, loop)
    }

    private fun startPlaylist(pages: List<DashboardPage>, loop: Boolean) {
        dashboardPages = pages
        loopPlaylist = loop
        currentIndex = 0
        isPlaying = true

        adminPanel.visibility = View.GONE
        webView.visibility = View.VISIBLE

        loadCurrentDashboard()
    }

    private fun parsePlaylistText(text: String): List<DashboardPage> {
        return text
            .lines()
            .mapNotNull { line ->
                val trimmed = line.trim()

                if (trimmed.isBlank()) {
                    return@mapNotNull null
                }

                val parts = trimmed.split("|").map { it.trim() }

                if (parts.size < 3) {
                    return@mapNotNull null
                }

                val title = parts[0]
                val url = parts[1]
                val seconds = parts[2].toLongOrNull() ?: 30L

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return@mapNotNull null
                }

                DashboardPage(
                    title = title.ifBlank { "Dashboard" },
                    url = url,
                    displaySeconds = seconds.coerceIn(5, 3600)
                )
            }
    }

    private fun loadCurrentDashboard() {
        if (dashboardPages.isEmpty()) {
            showAdminPanel()
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
            showAdminPanel()
            return
        }

        val isLastPage = currentIndex == dashboardPages.lastIndex

        if (isLastPage && !loopPlaylist) {
            // Play-once mode: remain on the final dashboard.
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

    private fun showAdminPanel() {
        isPlaying = false
        handler.removeCallbacks(rotateRunnable)

        webView.stopLoading()
        webView.visibility = View.GONE
        adminPanel.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isPlaying) {
            // Temporary POC behaviour:
            // Back returns to admin screen.
            // Later this will ask for admin PIN instead.
            showAdminPanel()
        } else {
            super.onBackPressed()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (isPlaying) {
                showAdminPanel()
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val KEY_PLAYLIST_TEXT = "playlist_text"
        private const val KEY_LOOP_PLAYLIST = "loop_playlist"
    }
}