package com.agrosense.app.mapper

import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.timeprovider.TimeProvider

class ReadingMapper(private val timeProvider: TimeProvider): Mapper<TemperatureMessage, TemperatureReading> {

    override fun map(arg: TemperatureMessage, id: Long?): TemperatureReading {
        return TemperatureReading(
            null,
            arg.value,
            timeProvider.now().plus(arg.timestamp),
            id!!
        )
    }
}