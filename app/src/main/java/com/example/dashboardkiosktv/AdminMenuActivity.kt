package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistStorage
import android.view.WindowManager
import com.example.dashboardkiosktv.kiosk.KioskManager
import com.example.dashboardkiosktv.security.AdminPinDialog

class AdminMenuActivity : AppCompatActivity() {

    private lateinit var managePlaylistButton: Button
    private lateinit var settingsButton: Button
    private lateinit var startDisplayButton: Button

    private lateinit var exitButton: Button

    private fun askPinAndExit() {
        AdminPinDialog.show(
            context = this,
            title = "Exit App",
            message = "Enter admin PIN to exit kiosk mode"
        ) {
            KioskManager.stopLockTaskIfActive(this)

            finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_menu)
        KioskManager.configureLockTaskIfDeviceOwner(this)
        KioskManager.startLockTaskIfPermitted(this)

        managePlaylistButton = findViewById(R.id.managePlaylistButton)
        settingsButton = findViewById(R.id.settingsButton)
        startDisplayButton = findViewById(R.id.startDisplayButton)
        exitButton = findViewById(R.id.exitButton)

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

        exitButton.setOnClickListener {
            askPinAndExit()
        }
    }
}