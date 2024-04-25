package com.agrosense.app.monitoring

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.agrosense.app.R
import com.agrosense.app.domain.entity.TemperatureReading

class ThresholdExceedanceHandler(private val context: Context) : ThresholdExceedanceListener {
    override fun onThresholdExceeded(reading: TemperatureReading) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "THRESHOLD")
            .setSmallIcon(R.drawable.ic_warning_24)
            .setContentTitle("Temperature exceeded!")
            .setContentText("The temperature reached${reading.value}!")
            .setPriority(PRIORITY_HIGH)
            .build()


        notificationManager.notify(1, notification)

    }
}