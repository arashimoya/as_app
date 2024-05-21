package com.agrosense.app.ui.views.linechart

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.flow.Flow

class LineChartViewModel(measurementDao: MeasurementDao) : ViewModel() {

    val temperatureReading: Flow<List<TemperatureReading>> = measurementDao.loadReadingsByMeasurement(1)
}