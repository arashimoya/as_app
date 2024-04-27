package com.agrosense.app.permission

import android.app.Activity
import androidx.core.app.ActivityCompat
import org.junit.Ignore
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock

@Ignore("cannot mock static methods? <nerd emoji>")
class PermissionHelperTest {

    private val permissionHelper: IPermissionHelper = PermissionHelper()
    private val mockActivity: Activity = mock()
    private val mockActivityCompat: MockedStatic<ActivityCompat> = mockStatic(ActivityCompat::class.java)

    @Test
    fun `should grant all BLE permissions`() {

        permissionHelper.grantBLEPermissions(mockActivity)

//        mockActivityCompat.verify { ActivityCompat.requestPermissions(mockActivity,permissionHelper) }

    }

    @Test
    fun `should grant notifications permissions`() {

        permissionHelper.grantNotificationPermissions(mockActivity)
    }
}