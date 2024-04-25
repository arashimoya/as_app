package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.TemperatureReading

class ConcreteThresholdNotifier: ThresholdNotifier {


    private val listeners = mutableListOf<ThresholdExceedanceListener>()

    override fun addListener(listener: ThresholdExceedanceListener) {
        listeners.add(listener)
    }

    override fun notifyThresholdExceeded(reading: TemperatureReading) {
        listeners.forEach { it.onThresholdExceeded(reading) }
    }
}