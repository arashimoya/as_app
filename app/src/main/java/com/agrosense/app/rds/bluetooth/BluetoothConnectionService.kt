package com.agrosense.app.rds.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.util.UUID

class BluetoothConnectionService : Service() {

    private val binder = LocalBinder()
    private var connectThread: ConnectThread? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothCommunicationService: BluetoothCommunicationService

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothConnectionService = this@BluetoothConnectionService
    }

    override fun onBind(intent: Intent): IBinder {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        return binder
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        connectThread?.cancel()
        device.createBond()

        connectThread = ConnectThread(device).apply { start() }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        connectThread?.cancel()
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            }
            bluetoothAdapter.cancelDiscovery()
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            socket?.let { socket ->
                socket.connect()
                bluetoothCommunicationService.read(socket)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    companion object{
        private const val TAG = "BluetoothConnectionService"
    }

}