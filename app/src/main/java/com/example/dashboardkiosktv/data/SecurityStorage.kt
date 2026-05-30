package com.example.dashboardkiosktv.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityStorage(context: Context) {

    private val prefs = context.getSharedPreferences(
        "dashboard_kiosk_security",
        Context.MODE_PRIVATE
    )

    fun hasAdminPin(): Boolean {
        return prefs.contains(KEY_PIN_SALT) && prefs.contains(KEY_PIN_HASH)
    }

    fun saveAdminPin(pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)

        val saltHex = saltBytes.toHexString()
        val hashHex = hashPin(pin, saltHex)

        prefs.edit()
            .putString(KEY_PIN_SALT, saltHex)
            .putString(KEY_PIN_HASH, hashHex)
            .apply()
    }

    fun verifyAdminPin(pin: String): Boolean {
        if (pin.isBlank()) return false

        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val enteredHash = hashPin(pin, salt)

        return enteredHash == savedHash
    }

    private fun hashPin(pin: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$saltHex:$pin".toByteArray(Charsets.UTF_8))
        return bytes.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_SALT = "admin_pin_salt"
        private const val KEY_PIN_HASH = "admin_pin_hash"
    }
}