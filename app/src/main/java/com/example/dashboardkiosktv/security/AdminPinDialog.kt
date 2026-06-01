package com.example.dashboardkiosktv.security

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.dashboardkiosktv.data.SecurityStorage

object AdminPinDialog {

    fun show(
        context: Context,
        title: String = "Admin Access",
        message: String = "Enter admin PIN",
        onCorrectPin: () -> Unit
    ) {
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Admin PIN"
            textSize = 20f
            setPadding(32, 24, 32, 24)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)

            addView(TextView(context).apply {
                text = message
                textSize = 18f
            })

            addView(input)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(container)
            .setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            .create()

        fun tryUnlock() {
            val enteredPin = input.text.toString().trim()
            val securityStorage = SecurityStorage(context)

            if (securityStorage.verifyAdminPin(enteredPin)) {
                dialog.dismiss()
                onCorrectPin()
            } else {
                input.setText("")
                Toast.makeText(
                    context,
                    "Incorrect PIN",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        input.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE

            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_UP

            if (isDoneAction || isEnterKey) {
                tryUnlock()
                true
            } else {
                false
            }
        }

        dialog.setOnShowListener {
            input.requestFocus()
            dialog.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            )
        }

        dialog.show()
    }
}