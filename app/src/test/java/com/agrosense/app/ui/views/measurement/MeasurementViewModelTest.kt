package com.agrosense.app.ui.views.measurement

import app.cash.turbine.test
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class MeasurementViewModelTest {

    private var measurementDao: MeasurementDao = Mockito.mock(MeasurementDao::class.java)

    private lateinit var viewModel: MeasurementViewModel

    @Test
    fun `should return null reading correctly `() = runTest {
        Mockito.`when`(measurementDao.loadLastReadingForMeasurement())
            .thenReturn(flowOf(null))
        viewModel = MeasurementViewModel(measurementDao)

        viewModel.lastTemperatureReading.test {
            assertEquals(null, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should return the reading correctly `() = runTest {
        Mockito.`when`(measurementDao.loadLastReadingForMeasurement())
            .thenReturn(flowOf(reading))
        viewModel = MeasurementViewModel(measurementDao)

        viewModel.lastTemperatureReading.test {
            assertEquals(reading, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should handle exception gracefully`() = runTest {
        val exception = RuntimeException("Database error")
        Mockito.`when`(measurementDao.loadMeasurements()).thenReturn(flow {
            throw exception
        })
        viewModel = MeasurementViewModel(measurementDao)

        viewModel.lastTemperatureReading.test {
            awaitError()
            cancelAndConsumeRemainingEvents()
        }
    }



    companion object{
        private val reading = TemperatureReading(27.0, DateTime.now(), 1)
    }
}