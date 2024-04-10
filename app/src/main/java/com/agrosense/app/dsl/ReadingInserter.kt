package com.agrosense.app.dsl

import android.util.Log
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.dsl.dao.ReadingDao
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.mapper.Mapper
import com.agrosense.app.mapper.ReadingMapper
import com.agrosense.app.timeprovider.CurrentTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ReadingInserter(
    private val mapper: Mapper<TemperatureMessage, TemperatureReading>,
    private val measurementDao: MeasurementDao?,
    private val readingDao: ReadingDao?
) {


    fun insert(messages: List<TemperatureMessage>) {
        val measurementOpt = measurementDao?.loadLastNotFinishedMeasurement()

        measurementOpt?.let { measurement ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    readingDao?.insertTemperatureReadings(*map(messages, measurement))
                } catch (e: Exception) {
                    logError("Error inserting temperature readings", e)
                }
            }
        } ?: logError("Could not find active measurement to save reading to")

    }

    private fun map(
        messages: List<TemperatureMessage>,
        measurement: Measurement
    ): Array<TemperatureReading> {
        return messages.map {
            mapper.map(
                it,
                measurement.measurementId!!
            )
        }.toTypedArray()
    }

    private fun logError(msg: String, e: java.lang.Exception? = null) {
        Log.e(TAG, msg, e)
    }


    companion object {
        const val TAG = "ReadingInserter"
        fun fromActivity(activity: WeakReference<BluetoothActivity>): ReadingInserter {
            val db = activity.get()?.let { AgroSenseDatabase.getDatabase(it) }
            return ReadingInserter(
                ReadingMapper(CurrentTimeProvider()),
                db?.measurementDao(),
                db?.readingDao()
            )
        }
    }


}

