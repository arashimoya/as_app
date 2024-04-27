package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.TemperatureReading

interface IThresholdNotifier {

    fun addListener(listener: ThresholdExceedanceListener)

    fun notifyThresholdExceeded(reading: TemperatureReading)
}