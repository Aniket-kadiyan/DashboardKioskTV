package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.PlaylistStorage
import com.example.dashboardkiosktv.data.SecurityStorage

class PinSetupActivity : AppCompatActivity() {

    private lateinit var pinInput: EditText
    private lateinit var confirmPinInput: EditText
    private lateinit var savePinButton: Button

    private lateinit var securityStorage: SecurityStorage
    private lateinit var playlistStorage: PlaylistStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        securityStorage = SecurityStorage(this)
        playlistStorage = PlaylistStorage(this)

        if (securityStorage.hasAdminPin()) {
            goToNextScreen()
            return
        }

        setContentView(R.layout.activity_pin_setup)

        pinInput = findViewById(R.id.pinInput)
        confirmPinInput = findViewById(R.id.confirmPinInput)
        savePinButton = findViewById(R.id.savePinButton)

        savePinButton.setOnClickListener {
            savePin()
        }
    }

    private fun savePin() {
        val pin = pinInput.text.toString().trim()
        val confirmPin = confirmPinInput.text.toString().trim()

        if (pin.length < 4) {
            Toast.makeText(
                this,
                "PIN must be at least 4 digits.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (pin != confirmPin) {
            Toast.makeText(
                this,
                "PINs do not match.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        securityStorage.saveAdminPin(pin)
        goToNextScreen()
    }

    private fun goToNextScreen() {
        val nextActivity = if (playlistStorage.hasValidSavedPlaylist()) {
            PlayerActivity::class.java
        } else {
            PlaylistActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }
}