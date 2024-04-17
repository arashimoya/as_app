package com.agrosense.app.mapper

import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.timeprovider.TimeProvider
import org.joda.time.DateTime
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ReadingMapperTest {

    private val timeProvider = mock(TimeProvider::class.java)
    private val mapper = ReadingMapper(timeProvider)

    @Test
    fun `should return a proper temperature reading entity`() {
        val message = TemperatureMessage(28.5, 500)
        val fixedDate = DateTime(2024, 4, 9, 12, 0, 0)//1712613600
        `when`(timeProvider.now()).thenReturn(fixedDate)

        val result = mapper.map(message, 1)

        assertEquals(fixedDate.plus(message.timestamp), result.recordedAt)

    }

    @Test
    fun `should return a proper temperature reading entity for timestamp 0`() {
        val message = TemperatureMessage(28.5, 0)
        val fixedDate = DateTime(2024, 4, 9, 12, 0, 0)//1712613600
        `when`(timeProvider.now()).thenReturn(fixedDate)

        val result = mapper.map(message, 1)

        assertEquals(fixedDate, result.recordedAt)

    }

    @Test
    fun `should return a proper temperature reading entity for a very large timestamp`() {
        val message = TemperatureMessage(28.5, 171261300)
        val fixedDate = DateTime(2024, 4, 9, 12, 0, 0)//1712613600
        `when`(timeProvider.now()).thenReturn(fixedDate)

        val result = mapper.map(message, 1)

        assertEquals(fixedDate.plus(message.timestamp), result.recordedAt)

    }

}