package com.agrosense.app.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.ui.views.linechart.LineChartViewModel

class LineChartViewModelFactory(private val measurementDao: MeasurementDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LineChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LineChartViewModel(measurementDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}