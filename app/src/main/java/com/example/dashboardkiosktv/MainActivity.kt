package com.example.dashboardkiosktv

import android.annotation.SuppressLint
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

data class DashboardPage(
    val title: String,
    val url: String,
    val displaySeconds: Long
)

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val handler = Handler(Looper.getMainLooper())

    private var currentIndex = 0

    private val dashboardPages = listOf(
        DashboardPage(
            title = "Test Page 1",
            url = "https://www.google.com",
            displaySeconds = 30
        ),
        DashboardPage(
            title = "Test Page 2",
            url = "https://www.wikipedia.org",
            displaySeconds = 30
        )
    )

    private val rotateRunnable = object : Runnable {
        override fun run() {
            showNextDashboard()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep the TV/emulator display awake while this activity is open.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Make the app occupy the whole screen.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.dashboardWebView)

        configureWebView()
        loadCurrentDashboard()
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
                // Keep all navigation inside this WebView.
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                // Start the timer only after the page finishes loading.
                scheduleNextDashboard()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)

                // For the POC, continue rotating even if one page fails.
                scheduleNextDashboard()
            }
        }
    }

    private fun loadCurrentDashboard() {
        if (dashboardPages.isEmpty()) return

        handler.removeCallbacks(rotateRunnable)

        val page = dashboardPages[currentIndex]
        webView.loadUrl(page.url)
    }

    private fun scheduleNextDashboard() {
        handler.removeCallbacks(rotateRunnable)

        val page = dashboardPages[currentIndex]
        handler.postDelayed(
            rotateRunnable,
            page.displaySeconds * 1000L
        )
    }

    private fun showNextDashboard() {
        if (dashboardPages.isEmpty()) return

        currentIndex = (currentIndex + 1) % dashboardPages.size
        loadCurrentDashboard()
    }

    override fun onBackPressed() {
        // For the POC, block accidental Back exits.
        // Later this will open the admin PIN dialog.
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // For now, block Back from remote/keyboard.
        // Later: long-press OK can open admin unlock.
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
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