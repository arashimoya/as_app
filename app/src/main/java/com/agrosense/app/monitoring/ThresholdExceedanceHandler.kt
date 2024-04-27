package com.agrosense.app.monitoring

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import com.agrosense.app.R
import com.agrosense.app.domain.entity.TemperatureReading

class ThresholdExceedanceHandler(private val context: Context) : ThresholdExceedanceListener {

    private var counter = 0
    @SuppressLint("MissingPermission")
    override fun onThresholdExceeded(reading: TemperatureReading) {

        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "THRESHOLD")
            .setSmallIcon(R.drawable.ic_warning_24)
            .setContentTitle("Temperature exceeded!")
            .setContentText("It reached ${String.format("%.2f",reading.value)}°C!")
            .setPriority(PRIORITY_HIGH)



        with(NotificationManagerCompat.from(context)) {
            Log.i("Handler", "notification for reading with value ${reading.value}")
            notify(counter, notification.build())
            counter++
        }

    }
}