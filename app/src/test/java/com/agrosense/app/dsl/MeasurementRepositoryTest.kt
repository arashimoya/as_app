package com.agrosense.app.dsl

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.timeprovider.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
@Ignore("This class works only if the tests are ran separately")
class MeasurementRepositoryTest {

    private var measurementDao: MeasurementDao = mock()
    private var timeProvider: TimeProvider = mock()

    private var repository = MeasurementRepository.getInstance(measurementDao, timeProvider)

    @Test
    fun `should call dao to insert measurement`(): Unit = runTest {
        val measurement = Measurement("name", now)
        repository.insertMeasurement(measurement)
        verify(measurementDao).insertMeasurements(measurement)
    }

    @Test
    fun `should call dao to update all measurements`() = runTest {
        `when`(timeProvider.now()).thenReturn(now)
        repository.updateEndForAllMeasurements()
        verify(measurementDao).updateAllMeasurementEndsToNow(now)
    }

    @Test
    fun `should insert measurements and update end values `() = runTest {
        val measurement = Measurement("not important", now)

        repository.insertNewMeasurement(measurement)

        verify(measurementDao).updateAllMeasurementEndsToNow(now)
        verify(measurementDao).insertMeasurements(measurement)

    }

    companion object {
        private val now: DateTime = DateTime(2024, 11, 12, 0, 0)
    }
}