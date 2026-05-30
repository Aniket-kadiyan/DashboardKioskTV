package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistStorage
import com.example.dashboardkiosktv.data.SecurityStorage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val securityStorage = SecurityStorage(this)
        val playlistStorage = PlaylistStorage(this)

        val nextActivity = when {
            !securityStorage.hasAdminPin() -> {
                PinSetupActivity::class.java
            }

            playlistStorage.hasValidSavedPlaylist() -> {
                PlayerActivity::class.java
            }

            else -> {
                AdminMenuActivity::class.java
            }
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }
}