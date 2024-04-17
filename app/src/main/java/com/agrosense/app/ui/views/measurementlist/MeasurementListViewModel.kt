package com.agrosense.app.ui.views.measurementlist

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import kotlinx.coroutines.flow.Flow

class MeasurementListViewModel(measurementDao : MeasurementDao)  : ViewModel() {

    val measurements: Flow<List<Measurement>> = measurementDao.loadMeasurements()
}