package com.agrosense.app.ui.main

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothDeviceViewModel : ViewModel() {
    private val _devices = MutableLiveData<List<BluetoothDevice>>(listOf())
    val devices: LiveData<List<BluetoothDevice>> = _devices

    fun addDevice(device: BluetoothDevice) {
        val currentList = _devices.value ?: emptyList()
        if (!currentList.contains(device)) {
            val updatedList = currentList + device
            _devices.value = updatedList
        }
    }

}