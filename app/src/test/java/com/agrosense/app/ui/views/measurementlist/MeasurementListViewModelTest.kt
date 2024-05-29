package com.agrosense.app.ui.views.measurementlist

import app.cash.turbine.test
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.rds.bluetooth.BluetoothConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class MeasurementListViewModelTest{


    private var measurementDao: MeasurementDao = mock(MeasurementDao::class.java)
    private var btConnectionState: BluetoothConnectionState = mock(BluetoothConnectionState::class.java)

    private lateinit var viewModel: MeasurementListViewModel

    @Test
    fun `should return one measurement`() = runTest {
        `when`(measurementDao.loadMeasurements()).thenReturn(flowOf(listOf(measurement)))
        viewModel = MeasurementListViewModel(measurementDao, btConnectionState)

        viewModel.measurements.test {
            assertEquals(listOf(measurement), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should return empty list of measurements`() = runTest {
        `when`(measurementDao.loadMeasurements()).thenReturn(flowOf(emptyList()))
        viewModel = MeasurementListViewModel(measurementDao, btConnectionState)

        viewModel.measurements.test {
            assertEquals(emptyList<Measurement>(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should return many measurements`() = runTest {
        val manyMeasurements = List(1000) { Measurement("test-$it", DateTime.now()) }
        `when`(measurementDao.loadMeasurements()).thenReturn(flowOf(manyMeasurements))
        viewModel = MeasurementListViewModel(measurementDao, btConnectionState)

        viewModel.measurements.test {
            assertEquals(manyMeasurements, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should handle exception gracefully`() = runTest {
        val exception = RuntimeException("Database error")
        `when`(measurementDao.loadMeasurements()).thenReturn(flow {
            throw exception
        })
        viewModel = MeasurementListViewModel(measurementDao, btConnectionState)

        viewModel.measurements.test {
            awaitError()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should return the state flow correctly`() = runTest {
        `when`(btConnectionState.isConnected()).thenReturn(flowOf(true))
        viewModel = MeasurementListViewModel(measurementDao, btConnectionState)

        viewModel.isConnectedToIOT.test {
            assertEquals(true, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    companion object{
       private val measurement = Measurement("test", DateTime.now())
    }
}