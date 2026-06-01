package com.example.dashboardkiosktv.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import com.example.dashboardkiosktv.MainActivity

object KioskManager {

    private const val TAG = "KioskManager"

    fun adminComponent(context: Context): ComponentName {
        return ComponentName(context, KioskDeviceAdminReceiver::class.java)
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun configureLockTaskIfDeviceOwner(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (!isDeviceOwner(context)) {
            Log.w(TAG, "App is not device owner. Lock task cannot be configured.")
            return
        }

        dpm.setLockTaskPackages(
            adminComponent(context),
            arrayOf(context.packageName)
        )

        Log.i(TAG, "Lock task package allowlisted: ${context.packageName}")
    }

    fun isLockTaskPermitted(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isLockTaskPermitted(context.packageName)
    }

    fun startLockTaskIfPermitted(activity: Activity) {
        if (!isLockTaskPermitted(activity)) {
            Log.w(TAG, "Lock task not permitted. Not starting lock task.")
            return
        }

        if (isInLockTaskMode(activity)) {
            return
        }

        try {
            activity.startLockTask()
            Log.i(TAG, "Lock task mode started.")
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to start lock task mode.", ex)
        }
    }

    fun stopLockTaskIfActive(activity: Activity) {
        if (!isInLockTaskMode(activity)) {
            return
        }

        try {
            activity.stopLockTask()
            Log.i(TAG, "Lock task mode stopped.")
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to stop lock task mode.", ex)
        }
    }

    fun isInLockTaskMode(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        return activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }

    fun setAsPersistentHomeIfDeviceOwner(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (!isDeviceOwner(context)) {
            Log.w(TAG, "App is not device owner. Cannot set persistent home.")
            return
        }

        val filter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        val activity = ComponentName(context, MainActivity::class.java)

        dpm.addPersistentPreferredActivity(
            adminComponent(context),
            filter,
            activity
        )

        Log.i(TAG, "Persistent home set to MainActivity.")
    }

    fun enableStayAwakeWhilePluggedInIfDeviceOwner(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (!isDeviceOwner(context)) {
            Log.w(TAG, "App is not device owner. Cannot set stay-awake policy.")
            return
        }

        val pluggedModes =
            BatteryManager.BATTERY_PLUGGED_AC or
                    BatteryManager.BATTERY_PLUGGED_USB or
                    BatteryManager.BATTERY_PLUGGED_WIRELESS

        dpm.setGlobalSetting(
            adminComponent(context),
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            pluggedModes.toString()
        )

        Log.i(TAG, "Stay awake while plugged in enabled.")
    }

    fun configureKioskPoliciesIfDeviceOwner(context: Context) {
        if (!isDeviceOwner(context)) {
            return
        }

        configureLockTaskIfDeviceOwner(context)
        setAsPersistentHomeIfDeviceOwner(context)
        enableStayAwakeWhilePluggedInIfDeviceOwner(context)
    }

    fun enterKioskModeIfPossible(activity: Activity) {
        configureKioskPoliciesIfDeviceOwner(activity)
        startLockTaskIfPermitted(activity)
    }

    fun getStayAwakeSetting(context: Context): String {
        return try {
            Settings.Global.getString(
                context.contentResolver,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN
            ) ?: "Not set"
        } catch (ex: Exception) {
            "Unavailable"
        }
    }
}