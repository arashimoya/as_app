package com.agrosense.app.ui.views.measurementlist

import androidx.lifecycle.ViewModel
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.rds.bluetooth.BluetoothConnectionState
import kotlinx.coroutines.flow.Flow

class MeasurementListViewModel(measurementDao : MeasurementDao, btConnectionState: BluetoothConnectionState)  : ViewModel() {

    val measurements: Flow<List<Measurement>> = measurementDao.loadMeasurements()

    val isConnectedToIOT: Flow<Boolean> = btConnectionState.isConnected()
}