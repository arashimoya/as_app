package com.agrosense.app.mapper

import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.timeprovider.TimeProvider

class ReadingMapper(private val timeProvider: TimeProvider) {

    fun map(message: TemperatureMessage, measurementId: Long): TemperatureReading {
        return TemperatureReading(
            null,
            message.value,
            timeProvider.now().plus(message.timestamp),
            measurementId
        )
    }
}