package com.agrosense.app

import android.util.Log
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.mapper.ReadingMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ReadingInserter(activity: WeakReference<BluetoothActivity>) {

    private val mapper = ReadingMapper()
    private val measurementDao = activity.get()
        ?.let { AgroSenseDatabase.getDatabase(it).measurementDao() }
    private val readingDao = activity.get()?.let { AgroSenseDatabase.getDatabase(it).readingDao() }

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
                measurement
            )
        }.toTypedArray()
    }

    private fun logError(msg: String, e: java.lang.Exception? = null) {
        Log.e(TAG, msg, e)
    }


    companion object {
        const val TAG = "ReadingInserter"
    }


}

