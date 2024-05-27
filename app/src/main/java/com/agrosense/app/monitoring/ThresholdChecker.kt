package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading

class ThresholdChecker(private val notifier: IThresholdNotifier) : IThresholdChecker {

    override fun check(readings: List<TemperatureReading>, measurement: Measurement) {
        readings.forEach {
            if (maxExceeded(measurement, it) || minExceeded(measurement, it)) {
                notifier.notifyThresholdExceeded(it)
            }
        }
    }

    private fun minExceeded(
        measurement: Measurement,
        it: TemperatureReading,
    ) = measurement.minValue != null && it.value < measurement.minValue

    private fun maxExceeded(
        measurement: Measurement,
        it: TemperatureReading,
    ) = measurement.maxValue != null && it.value > measurement.maxValue

}