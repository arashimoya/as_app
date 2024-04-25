package com.agrosense.app

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.dsl.ReadingInserter
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.dsl.dao.ReadingDao
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.mapper.Mapper
import com.agrosense.app.monitoring.IThresholdChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class ReadingInserterTest {


    private lateinit var readingInserter: ReadingInserter
    private val measurementDao: MeasurementDao = mock()
    private val readingDao: ReadingDao = mock()
    private val mockDatabase: AgroSenseDatabase = mock()
    private val mockMapper: Mapper<TemperatureMessage, TemperatureReading> = mock()
    private val mockChecker: IThresholdChecker = mock()

    @Before
    fun setUp() {
        `when`(mockDatabase.measurementDao()).thenReturn(measurementDao)
        `when`(mockDatabase.readingDao()).thenReturn(readingDao)

        readingInserter = ReadingInserter(
            mockMapper,
            measurementDao, readingDao, mockChecker
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should log if measurement is null`() = runTest {
        `when`(measurementDao.loadLastNotFinishedMeasurement()).thenReturn(null)
        val list = listOf<TemperatureMessage>()

        readingInserter.insert(list)

        verify(readingDao, never()).insertTemperatureReadings(anyOrNull())
        verify(mockChecker, never()).check(anyOrNull(), anyOrNull())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should insert readings `() = runTest {
        val toMap = TemperatureMessage(28.5, 452)
        val list = listOf(toMap)
        val measurement = Measurement(1, "not important", DateTime(1712728000), null, 30.00, 0.00)
        `when`(measurementDao.loadLastNotFinishedMeasurement()).thenReturn(measurement)
        `when`(mockMapper.map(toMap, measurement.measurementId!!)).thenReturn(
            TemperatureReading(
                null,
                toMap.value,
                measurement.start.plus(toMap.timestamp),
                measurement.measurementId!!
            )
        )

        readingInserter.insert(list)

        verify(readingDao).insertTemperatureReadings(anyOrNull())
        verify(mockChecker).check(anyOrNull(), anyOrNull())
    }
}