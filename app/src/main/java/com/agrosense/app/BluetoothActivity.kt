package com.agrosense.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.bluetooth.BluetoothService
import com.agrosense.app.bluetooth.MESSAGE_READ
import com.agrosense.app.bluetooth.MESSAGE_TOAST
import com.agrosense.app.bluetooth.MESSAGE_WRITE
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.UUID


val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

class BluetoothActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private lateinit var bluetoothService: BluetoothService
    private var connectThread: ConnectThread? = null


    private val handler: Handler = MyHandler(this)


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        if (!hasAllPermissions(this)) {
            grantPermissions()
        }

        bluetoothService = BluetoothService(handler)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        recyclerView = findViewById(R.id.devices)

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondReceiver, filter)

        val discoverFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverFilter)

        val button = findViewById<Button>(R.id.send)
        val sendTextView = findViewById<EditText>(R.id.send_message)

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        bluetoothAdapter.startDiscovery()

        button.setOnClickListener {
            val message = sendTextView.text.toString()
            bluetoothService.write(message.toByteArray())
        }

        adapter = DeviceAdapter(devices, ::connect)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun refreshAdapter(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            recyclerView.adapter?.notifyItemInserted(devices.indexOf(device))
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        if (!hasAllPermissions(this)) {
            grantPermissions()
        }
        Log.d(TAG, "connect: Trying to pair with " + device.name)

        connectThread = ConnectThread(device)
        connectThread?.start()

    }

    private val discoverReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    Log.d(TAG, "discoverReceiver: found $device")
                    refreshAdapter(device)
                }
            }
        }
    }

    private val bondReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    when ((intent.extras?.get(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice).bondState) {
                        BluetoothDevice.BOND_BONDED -> Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                        BluetoothDevice.BOND_BONDING -> Log.d(
                            TAG,
                            "BroadcastReceiver: BOND_BONDING."
                        )

                        BluetoothDevice.BOND_NONE -> Log.d(TAG, "BroadcastReceiver: BOND_NONE.")
                    }

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(bondReceiver)
        unregisterReceiver(discoverReceiver)
        connectThread?.cancel()
        bluetoothService.stopConnection()
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if(!bluetoothAdapter.isEnabled){
                bluetoothAdapter.enable()
            }
            bluetoothAdapter.cancelDiscovery()
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            socket?.let { socket ->
                socket.connect()
                bluetoothService.read(socket)
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

    private fun grantPermissions() {
        ActivityCompat.requestPermissions(this, ALL_BLE_PERMISSIONS, 2)
    }

    private fun hasAllPermissions(context: Context) =
        ALL_BLE_PERMISSIONS.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private class MyHandler(activity: BluetoothActivity) : Handler() {
        private val activityReference = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = activityReference.get()
            activity?.let {
                when (msg.what) {
                    MESSAGE_READ -> {
                        val bytes = msg.obj as ByteArray
                        val readMessage = String(bytes, 0, msg.arg1)
                        Toast.makeText(activity, readMessage, Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "Message read: $readMessage")
                    }

                    MESSAGE_WRITE -> {

                    }

                    MESSAGE_TOAST -> {
                        val toastMessage = msg.data.getString("toast")
                        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }
    }


    companion object {
        private const val TAG: String = "BluetoothActivity"
        private val devices: ArrayList<BluetoothDevice> = ArrayList()
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    }
}

