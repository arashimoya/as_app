package com.agrosense.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.dsl.dao.MeasurementDao

class MeasurementViewModelFactory(private val dao: MeasurementDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeasurementViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}