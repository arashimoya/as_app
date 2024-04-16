package com.agrosense.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeasurementViewModel(private val measurementDao: MeasurementDao) : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.IO)


    fun getReadingsByMeasurement(measurementId: Long): LiveData<List<TemperatureReading>> {
        return measurementDao.loadReadingsByMeasurement(measurementId).asLiveData()
    }

    fun insertMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            measurementDao.insertMeasurements(measurement)
        }
    }


}
