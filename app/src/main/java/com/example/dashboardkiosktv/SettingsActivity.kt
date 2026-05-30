package com.example.dashboardkiosktv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.SecurityStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var currentPinInput: EditText
    private lateinit var newPinInput: EditText
    private lateinit var confirmNewPinInput: EditText
    private lateinit var changePinButton: Button
    private lateinit var backButton: Button

    private lateinit var securityStorage: SecurityStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        securityStorage = SecurityStorage(this)

        currentPinInput = findViewById(R.id.currentPinInput)
        newPinInput = findViewById(R.id.newPinInput)
        confirmNewPinInput = findViewById(R.id.confirmNewPinInput)
        changePinButton = findViewById(R.id.changePinButton)
        backButton = findViewById(R.id.backButton)

        changePinButton.setOnClickListener {
            changePin()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun changePin() {
        val currentPin = currentPinInput.text.toString().trim()
        val newPin = newPinInput.text.toString().trim()
        val confirmNewPin = confirmNewPinInput.text.toString().trim()

        if (!securityStorage.verifyAdminPin(currentPin)) {
            Toast.makeText(
                this,
                "Current PIN is incorrect.",
                Toast.LENGTH_LONG
            ).show()
            currentPinInput.setText("")
            return
        }

        if (newPin.length < 4) {
            Toast.makeText(
                this,
                "New PIN must be at least 4 digits.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (newPin != confirmNewPin) {
            Toast.makeText(
                this,
                "New PINs do not match.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        securityStorage.saveAdminPin(newPin)

        currentPinInput.setText("")
        newPinInput.setText("")
        confirmNewPinInput.setText("")

        Toast.makeText(
            this,
            "Admin PIN changed successfully.",
            Toast.LENGTH_LONG
        ).show()
    }
}