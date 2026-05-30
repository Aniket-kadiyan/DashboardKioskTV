package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistStorage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = PlaylistStorage(this)

        val nextActivity = if (storage.hasValidSavedPlaylist()) {
            PlayerActivity::class.java
        } else {
            PlaylistActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }
}