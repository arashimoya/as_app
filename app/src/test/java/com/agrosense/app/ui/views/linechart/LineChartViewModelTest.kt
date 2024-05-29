package com.agrosense.app.ui.views.linechart

import app.cash.turbine.test
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LineChartViewModelTest {

    private var measurementDao = mock(MeasurementDao::class.java)
    private var viewModel = LineChartViewModel(measurementDao)

    @Test
    fun `should get empty list of temperature readings from dao`() = runTest {
        `when`(measurementDao.loadReadingsByMeasurement(1)).thenReturn(flowOf(emptyList()))

        viewModel.getTemperatureReadings(1).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
        verify(measurementDao).loadReadingsByMeasurement(1)

    }

    @Test
    fun `should get a list with one temperature reading from dao`() = runTest {
        `when`(measurementDao.loadReadingsByMeasurement(1)).thenReturn(flowOf(listOf(reading)))

        viewModel.getTemperatureReadings(1).test {
            assertEquals(listOf(reading), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        verify(measurementDao).loadReadingsByMeasurement(1)

    }

    @Test
    fun `should get a list with many temperature readings from dao`() = runTest {
        val readings = List(1000){TemperatureReading(25.0, DateTime.now(), 1)}
        `when`(measurementDao.loadReadingsByMeasurement(1)).thenReturn(flowOf(readings))

        viewModel.getTemperatureReadings(1).test {
            assertEquals(readings, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        verify(measurementDao).loadReadingsByMeasurement(1)

    }

    @Test
    fun `should handle error gracefully when getting readings`() = runTest {

        `when`(measurementDao.loadReadingsByMeasurement(1)).thenReturn(flow {
            throw exception
        })

        viewModel.getTemperatureReadings(1).test {
            awaitError()
            cancelAndConsumeRemainingEvents()
        }
        verify(measurementDao).loadReadingsByMeasurement(1)

    }

    @Test
    fun `should get measurement given a proper id`() = runTest {
        `when`(measurementDao.getMeasurement(1)).thenReturn(measurement)

        val result  = viewModel.getMeasurement(1)

        assertEquals(measurement, result)
        verify(measurementDao).getMeasurement(1)
    }

    @Test
    fun `should throw exception if measurementDao throws one`() = runTest {
        whenever(measurementDao.getMeasurement(1)).thenThrow(exception)

        try {
            viewModel.getMeasurement(1)
            fail("Expected RuntimeException was not thrown.")
        } catch (e: RuntimeException) {
            assertNotNull(e.message)
            assertEquals(exception.message, e.message)
        }
    }



    companion object{
        private val reading = TemperatureReading(25.0, DateTime.now(), 1)
        private val measurement = Measurement("test", DateTime.now())
        private val exception = RuntimeException("Database error")
    }
}