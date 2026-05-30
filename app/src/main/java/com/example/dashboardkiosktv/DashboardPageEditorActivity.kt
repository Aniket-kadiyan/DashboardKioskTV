package com.example.dashboardkiosktv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardPageEditorActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var urlInput: EditText
    private lateinit var durationInput: EditText
    private lateinit var saveItemButton: Button
    private lateinit var cancelButton: Button

    private var editIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard_page_editor)

        titleInput = findViewById(R.id.titleInput)
        urlInput = findViewById(R.id.urlInput)
        durationInput = findViewById(R.id.durationInput)
        saveItemButton = findViewById(R.id.saveItemButton)
        cancelButton = findViewById(R.id.cancelButton)

        editIndex = intent.getIntExtra(EXTRA_INDEX, -1)

        titleInput.setText(intent.getStringExtra(EXTRA_TITLE) ?: "")
        urlInput.setText(intent.getStringExtra(EXTRA_URL) ?: "")
        durationInput.setText(
            intent.getLongExtra(EXTRA_DURATION_SECONDS, 30L).toString()
        )

        saveItemButton.setOnClickListener {
            saveItem()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveItem() {
        val title = titleInput.text.toString().trim().ifBlank { "Dashboard" }
        val url = urlInput.text.toString().trim()
        val duration = durationInput.text.toString().trim().toLongOrNull() ?: 30L

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(
                this,
                "URL must start with http:// or https://",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_INDEX, editIndex)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_URL, url)
            putExtra(EXTRA_DURATION_SECONDS, duration.coerceIn(5, 3600))
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val EXTRA_INDEX = "extra_index"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }
}