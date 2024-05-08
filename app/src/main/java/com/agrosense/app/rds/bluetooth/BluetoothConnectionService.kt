package com.agrosense.app.rds.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException

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
        bluetoothCommunicationService = BluetoothCommunicationService(applicationContext)
        return binder
    }

    @SuppressLint("MissingPermission")
    fun connect(socket: BluetoothSocket) {
        connectThread?.cancel()
        connectThread = ConnectThread(socket).apply { start() }

    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        connectThread?.cancel()
    }

    fun isConnected(): Boolean {
        return if(connectThread == null){
            false
        } else {
            connectThread!!.isConnected()
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(val socket: BluetoothSocket) : Thread() {

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            socket.let { socket ->
                socket.connect()
                bluetoothCommunicationService.read(socket)
            }
        }

        fun isConnected() = socket.isConnected


        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    companion object{
        private const val TAG = "BluetoothConnectionService"
    }

}