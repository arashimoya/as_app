package com.agrosense.app.rds.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class BluetoothConnectionState private constructor() {

    private var isConnectedInternal = false
    private val isConnectedStateFlow = MutableSharedFlow<Boolean>()

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
        isConnectedStateFlow.tryEmit(isConnected)
    }

    fun isConnected(): Flow<Boolean> = flow {
        emitAll(isConnectedStateFlow)
        emit(isConnectedInternal)
    }
}
