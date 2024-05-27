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
        private const val START = "S"
        private const val STOP = "P"
    }
}
