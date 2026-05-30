package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistParser
import com.example.dashboardkiosktv.data.PlaylistStorage

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistInput: EditText
    private lateinit var loopCheckbox: CheckBox
    private lateinit var startButton: Button

    private lateinit var storage: PlaylistStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_playlist)

        storage = PlaylistStorage(this)

        playlistInput = findViewById(R.id.playlistInput)
        loopCheckbox = findViewById(R.id.loopCheckbox)
        startButton = findViewById(R.id.startButton)

        playlistInput.setText(storage.getPlaylistTextOrDefault())
        loopCheckbox.isChecked = storage.isLoopEnabled()

        startButton.setOnClickListener {
            saveAndStart()
        }
    }

    private fun saveAndStart() {
        val playlistText = playlistInput.text.toString()
        val loop = loopCheckbox.isChecked

        val parsedPages = PlaylistParser.parse(playlistText)

        if (parsedPages.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter at least one valid dashboard line.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        storage.savePlaylist(playlistText, loop)

        startActivity(Intent(this, PlayerActivity::class.java))
        finish()
    }
}