package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistStorage

class AdminMenuActivity : AppCompatActivity() {

    private lateinit var managePlaylistButton: Button
    private lateinit var settingsButton: Button
    private lateinit var startDisplayButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_menu)

        managePlaylistButton = findViewById(R.id.managePlaylistButton)
        settingsButton = findViewById(R.id.settingsButton)
        startDisplayButton = findViewById(R.id.startDisplayButton)

        managePlaylistButton.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsMenuActivity::class.java))
        }

        startDisplayButton.setOnClickListener {
            val storage = PlaylistStorage(this)

            if (!storage.hasValidSavedPlaylist()) {
                Toast.makeText(
                    this,
                    "Please create a valid playlist first.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, PlayerActivity::class.java))
            finish()
        }
    }
}