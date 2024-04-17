package com.agrosense.app.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.ui.views.measurement.MeasurementViewModel

class MeasurementViewModelFactory(private val measurementDao: MeasurementDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeasurementViewModel(measurementDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
