package com.example.dashboardkiosktv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsMenuActivity : AppCompatActivity() {

    private lateinit var changePinButton: Button
    private lateinit var kioskStatusButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings_menu)

        changePinButton = findViewById(R.id.changePinButton)
        kioskStatusButton = findViewById(R.id.kioskStatusButton)
        backButton = findViewById(R.id.backButton)

        changePinButton.setOnClickListener {
            startActivity(Intent(this, ChangePinActivity::class.java))
        }

        kioskStatusButton.setOnClickListener {
            startActivity(Intent(this, KioskStatusActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}