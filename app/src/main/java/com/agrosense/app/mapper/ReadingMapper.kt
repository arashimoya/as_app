package com.agrosense.app.mapper

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import org.joda.time.DateTime

class ReadingMapper {


    fun map(message: TemperatureMessage, measurement: Measurement): TemperatureReading {
        return TemperatureReading(
            null,
            message.value,
            DateTime.now().plus(message.timestamp),
            measurement.measurementId!!
        )
    }
}