package com.agrosense.app.ui.measurement

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.flow.Flow

class MeasurementViewModel(measurementDao : MeasurementDao) : ViewModel() {

    val lastTemperatureReading: Flow<TemperatureReading?> = measurementDao.loadLastReadingForMeasurement()

}