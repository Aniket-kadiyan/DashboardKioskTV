package com.example.dashboardkiosktv

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.kiosk.KioskManager

class KioskStatusActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var configureButton: Button
    private lateinit var refreshButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_kiosk_status)

        statusText = findViewById(R.id.statusText)
        configureButton = findViewById(R.id.configureButton)
        refreshButton = findViewById(R.id.refreshButton)
        backButton = findViewById(R.id.backButton)

        configureButton.setOnClickListener {
            KioskManager.configureKioskPoliciesIfDeviceOwner(this)
            Toast.makeText(
                this,
                "Kiosk policy configuration attempted.",
                Toast.LENGTH_SHORT
            ).show()
            refreshStatus()
        }

        refreshButton.setOnClickListener {
            refreshStatus()
        }

        backButton.setOnClickListener {
            finish()
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val isDeviceOwner = KioskManager.isDeviceOwner(this)
        val isLockTaskPermitted = KioskManager.isLockTaskPermitted(this)
        val isInLockTaskMode = KioskManager.isInLockTaskMode(this)
        val stayAwakeSetting = KioskManager.getStayAwakeSetting(this)

        statusText.text = buildString {
            appendLine("Device owner: ${yesNo(isDeviceOwner)}")
            appendLine("Lock task permitted: ${yesNo(isLockTaskPermitted)}")
            appendLine("Currently in lock task mode: ${yesNo(isInLockTaskMode)}")
            appendLine("Stay awake while plugged in: $stayAwakeSetting")
            appendLine()
            appendLine("Expected for full kiosk:")
            appendLine("Device owner: Yes")
            appendLine("Lock task permitted: Yes")
            appendLine("Currently in lock task mode: Yes while app is locked")
            appendLine("Stay awake while plugged in: non-zero value")
        }

        configureButton.isEnabled = isDeviceOwner
    }

    private fun yesNo(value: Boolean): String {
        return if (value) "Yes" else "No"
    }
}