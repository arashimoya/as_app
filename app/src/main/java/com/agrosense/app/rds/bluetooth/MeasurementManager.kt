package com.agrosense.app.rds.bluetooth

class MeasurementManager(private val bluetoothService: BluetoothCommunicationService) {

    fun start() {
        bluetoothService.write(START.toByteArray())
    }

    fun stop() {
        bluetoothService.write(STOP.toByteArray())
        bluetoothService.stopConnection()
    }

    companion object {
        const val START = "start"
        const val STOP = "stop"
    }
}
