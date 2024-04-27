package com.agrosense.app.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class PermissionHelper : IPermissionHelper {

    override fun grantBLEPermissions(context: Activity) {
        if (!hasAllBLEPermissions(context))
            ActivityCompat.requestPermissions(context, ALL_BLE_PERMISSIONS, 2)
    }

    override fun grantNotificationPermissions(context: Activity) {
        if (!hasNotificationPermission(context))
            ActivityCompat.requestPermissions(context, NOTIFICATIONS_PERMISSIONS, 2)
    }

    private fun hasAllBLEPermissions(context: Context) =
        ALL_BLE_PERMISSIONS.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun hasNotificationPermission(context: Context) =
        NOTIFICATIONS_PERMISSIONS.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}

val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

val NOTIFICATIONS_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
} else {
    arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
}