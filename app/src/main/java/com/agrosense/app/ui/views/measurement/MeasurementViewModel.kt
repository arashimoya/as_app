package com.agrosense.app.ui.views.measurement

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.MeasurementRepo
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.rds.bluetooth.MeasurementManager
import kotlinx.coroutines.flow.Flow

class MeasurementViewModel(
    measurementDao: MeasurementDao, private val measurementRepository: MeasurementRepo,
    private val measurementManager: MeasurementManager
) : ViewModel() {

    val lastTemperatureReading: Flow<TemperatureReading?> =
        measurementDao.loadLastReadingForMeasurement()

    fun stopMeasurement() {
        measurementRepository.updateEndForAllMeasurements()
        measurementManager.stop()
    }

}