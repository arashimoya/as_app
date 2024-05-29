package com.agrosense.app.dsl

import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.timeprovider.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeasurementRepository private constructor(
    private val measurementDao: MeasurementDao,
    private val timeProvider: TimeProvider
) : MeasurementRepo {

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
        fun getInstance(dao: MeasurementDao, timeProvider: TimeProvider): MeasurementRepo {
            if (instance == null) {
                instance = MeasurementRepository(
                    dao,
                    timeProvider
                )
            }
            return instance!!
        }
    }
}