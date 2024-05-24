package com.agrosense.app.monitoring

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.agrosense.app.domain.entity.TemperatureReading
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@Ignore
class ThresholdExceedanceHandlerTest{


    private var notificationManager: NotificationManager = mock()

    private var context: Context = mock()


    @Test
    fun `test onThresholdExceeded displays notification`() {
        // Given
        val handler = ThresholdExceedanceHandler(context)
        val reading = TemperatureReading(null ,value = 25.0, DateTime.now(), 1)
        Mockito.`when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)

        // When
        handler.onThresholdExceeded(reading)

        // Then
        val expectedNotification = NotificationCompat.Builder(context, "THRESHOLD")
//            .setSmallIcon(R.drawable.ic_warning_24)
            .setContentTitle("Temperature exceeded!")
            .setContentText("The temperature reached${reading.value}!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        verify(notificationManager).notify(1, expectedNotification)
    }
}
