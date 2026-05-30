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
import android.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.dashboardkiosktv.data.SecurityStorage

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

    private fun showAdminUnlockDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Admin PIN"
            textSize = 20f
            setPadding(32, 24, 32, 24)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)

            addView(TextView(this@PlayerActivity).apply {
                text = "Enter admin PIN"
                textSize = 18f
            })

            addView(input)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Admin Access")
            .setView(container)
            .setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            .create()

        fun tryUnlock() {
            val enteredPin = input.text.toString().trim()
            val securityStorage = SecurityStorage(this@PlayerActivity)

            if (securityStorage.verifyAdminPin(enteredPin)) {
                dialog.dismiss()
                openAdminMenu()
            } else {
                input.setText("")
                Toast.makeText(
                    this@PlayerActivity,
                    "Incorrect PIN",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        input.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction =
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE

            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_UP

            if (isDoneAction || isEnterKey) {
                tryUnlock()
                true
            } else {
                false
            }
        }

        dialog.setOnShowListener {
            input.requestFocus()
            dialog.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            )
        }

        dialog.show()
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
            openAdminMenu()
            return
        }

        val parsedPages = PlaylistParser.parse(playlistText)

        if (parsedPages.isEmpty()) {
            openAdminMenu()
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
            openAdminMenu()
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
            openAdminMenu()
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

    private fun openAdminMenu() {
        isPlayerActive = false
        handler.removeCallbacks(rotateRunnable)

        startActivity(Intent(this, AdminMenuActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showAdminUnlockDialog()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            showAdminUnlockDialog()
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