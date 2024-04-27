package com.agrosense.app.permission

import android.app.Activity

interface IPermissionHelper {

    fun grantBLEPermissions(context: Activity)
    fun grantNotificationPermissions(context: Activity)

}