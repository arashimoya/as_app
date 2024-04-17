package com.agrosense.app.dsl

import android.content.Context
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.timeprovider.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeasurementRepository private constructor(
    private val measurementDao: MeasurementDao,
    private val timeProvider: TimeProvider
) : MeasurementRepo {

    override fun insertMeasurement(measurement: Measurement) {
        CoroutineScope(Dispatchers.IO).launch {
            measurementDao.insertMeasurements(measurement)
        }
    }

    override fun updateEndForAllMeasurements() {
        CoroutineScope(Dispatchers.IO).launch {
            measurementDao.updateAllMeasurementEndsToNow(timeProvider.now())
        }
    }

    override fun insertNewMeasurement(measurement: Measurement) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Default) {
                measurementDao.updateAllMeasurementEndsToNow(
                    timeProvider.now()
                )
            }
            measurementDao.insertMeasurements(measurement)
        }
    }

    companion object {
        private var instance: MeasurementRepo? = null

        @Synchronized
        fun getInstance(context: Context, timeProvider: TimeProvider): MeasurementRepo {
            if (instance == null) {
                instance = MeasurementRepository(
                    AgroSenseDatabase.getDatabase(context).measurementDao(),
                    timeProvider
                )
            }
            return instance!!
        }
    }
}