package com.agrosense.app.dsl

import android.content.Context
import android.util.Log
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.dsl.dao.ReadingDao
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.mapper.Mapper
import com.agrosense.app.mapper.ReadingMapper
import com.agrosense.app.monitoring.ThresholdChecker
import com.agrosense.app.monitoring.ThresholdExceedanceHandler
import com.agrosense.app.monitoring.ConcreteThresholdNotifier
import com.agrosense.app.monitoring.IThresholdChecker
import com.agrosense.app.timeprovider.CurrentTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadingInserter(
    private val mapper: Mapper<TemperatureMessage, TemperatureReading>,
    private val measurementDao: MeasurementDao?,
    private val readingDao: ReadingDao?,
    private val thresholdChecker: IThresholdChecker
) {


    fun insert(messages: List<TemperatureMessage>) {
        val measurementOpt = measurementDao?.loadLastNotFinishedMeasurement()

        measurementOpt?.let { measurement ->
            val readings = map(messages, measurement)
            thresholdChecker.check(readings, measurement)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    readingDao?.insertTemperatureReadings(*readings.toTypedArray())
                } catch (e: Exception) {
                    logError("Error inserting temperature readings", e)
                }
            }
        } ?: logError("Could not find active measurement to save reading to")

    }

    private fun map(
        messages: List<TemperatureMessage>,
        measurement: Measurement
    ): List<TemperatureReading> {
        return messages.map {
            mapper.map(
                it,
                measurement.measurementId!!
            )
        }
    }

    private fun logError(msg: String, e: java.lang.Exception? = null) {
        Log.e(TAG, msg, e)
    }


    companion object {
        const val TAG = "ReadingInserter"
        fun fromContext(context: Context): ReadingInserter {
            val db =  AgroSenseDatabase.getDatabase(context)
            val notifier = ConcreteThresholdNotifier()
            notifier.addListener(ThresholdExceedanceHandler(context))
            val checker = ThresholdChecker(notifier)
            return ReadingInserter(
                ReadingMapper(CurrentTimeProvider()),
                db.measurementDao(),
                db.readingDao(),
                checker
            )
        }
    }


}

