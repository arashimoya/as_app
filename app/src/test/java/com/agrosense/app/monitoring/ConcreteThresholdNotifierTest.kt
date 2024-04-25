package com.agrosense.app.monitoring

import com.agrosense.app.domain.entity.TemperatureReading
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class ConcreteThresholdNotifierTest{

    @Test
    fun `test notifyThresholdExceeded notifies all listeners`() {

        val notifier = ConcreteThresholdNotifier()
        val listener1: ThresholdExceedanceListener = mock()
        val listener2: ThresholdExceedanceListener = mock()
        notifier.addListener(listener1)
        notifier.addListener(listener2)
        val reading = TemperatureReading(null ,value = 25.0, DateTime.now(), 1)

        // When
        notifier.notifyThresholdExceeded(reading)

        // Then
        verify(listener1).onThresholdExceeded(reading)
        verify(listener2).onThresholdExceeded(reading)
    }
}