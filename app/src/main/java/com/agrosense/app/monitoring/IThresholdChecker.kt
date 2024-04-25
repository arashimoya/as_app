package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading

interface IThresholdChecker {

    fun check(readings: List<TemperatureReading>, measurement: Measurement)
}