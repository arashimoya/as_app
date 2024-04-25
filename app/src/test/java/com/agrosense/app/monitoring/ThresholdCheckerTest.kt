package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull

class ThresholdCheckerTest {

     var notifier: ThresholdNotifier = mock()

    @Test
    fun `test check method notifies notifier when max threshold exceeded`() {
        // Given
        val checker = ThresholdChecker(notifier)
        val readings = listOf(
            TemperatureReading(null ,value = 30.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 25.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 35.0, DateTime.now(), 1),
        )
        val measurement = Measurement("test",  DateTime.now(), 28.0,null)

        // When
        checker.check(readings, measurement)

        // Then
        verify(notifier, times(2)).notifyThresholdExceeded(anyOrNull())
    }

    @Test
    fun `test check method notifies notifier when min threshold exceeded`() {
        // Given
        val checker = ThresholdChecker(notifier)
        val readings = listOf(
            TemperatureReading(null ,value = 20.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 10.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 15.0, DateTime.now(), 1),
        )
        val measurement = Measurement("test",  DateTime.now(), null,18.0)

        // When
        checker.check(readings, measurement)

        // Then
        verify(notifier, times(2)).notifyThresholdExceeded(anyOrNull())
    }

    @Test
    fun `test check method does not notify notifier when thresholds not exceeded`() {
        // Given
        val checker = ThresholdChecker(notifier)
        val readings = listOf(
            TemperatureReading(null ,value = 20.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 25.0, DateTime.now(), 1),
            TemperatureReading(null ,value = 22.0, DateTime.now(), 1),
        )
        val measurement = Measurement("test",  DateTime.now(), 28.0,18.0)

        // When
        checker.check(readings, measurement)

        // Then
        verify(notifier, never()).notifyThresholdExceeded(anyOrNull())
    }
}
