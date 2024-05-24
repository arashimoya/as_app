package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.TemperatureReading

interface ThresholdExceedanceListener {
    fun onThresholdExceeded(reading: TemperatureReading)
}