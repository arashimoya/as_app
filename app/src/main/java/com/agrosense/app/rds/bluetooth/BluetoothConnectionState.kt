package com.agrosense.app.rds.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothConnectionState private constructor() {

    private var isConnectedInternal = false
    private val isConnectedStateFlow = MutableStateFlow(false)  // default initial value

    companion object {
        @Volatile
        private var instance: BluetoothConnectionState? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: BluetoothConnectionState().also { instance = it }
            }
    }

    fun setDisconnected() {
        updateConnectionState(false)
    }

    fun setConnected() {
        updateConnectionState(true)
    }

    private fun updateConnectionState(isConnected: Boolean) {
        isConnectedInternal = isConnected
        isConnectedStateFlow.value = isConnected  // Update the state
    }

    fun isConnected(): Flow<Boolean> = isConnectedStateFlow.asStateFlow()
}
