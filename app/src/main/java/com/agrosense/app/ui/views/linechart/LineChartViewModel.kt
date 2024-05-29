package com.agrosense.app.ui.views.linechart

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LineChartViewModel(val measurementDao: MeasurementDao) : ViewModel() {

    fun getTemperatureReadings(measurementId: Long): Flow<List<TemperatureReading>> =
        measurementDao.loadReadingsByMeasurement(measurementId)

    suspend fun getMeasurement(measurementId: Long): Measurement =
        withContext(Dispatchers.IO) { measurementDao.getMeasurement(measurementId) }
}