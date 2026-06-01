package com.example.dashboardkiosktv.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log

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
}