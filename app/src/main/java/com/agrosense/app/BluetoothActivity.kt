package com.agrosense.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.bluetooth.BluetoothCommunicationService
import com.agrosense.app.bluetooth.MESSAGE_READ
import com.agrosense.app.bluetooth.MESSAGE_TOAST
import com.agrosense.app.bluetooth.MESSAGE_WRITE
import com.agrosense.app.ui.main.BluetoothDeviceViewModel
import com.agrosense.app.ui.main.BluetoothFragment
import java.lang.ref.WeakReference


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
    private lateinit var bluetoothCommunicationService: BluetoothCommunicationService

    private lateinit var deviceViewModel: BluetoothDeviceViewModel


    private val handler: Handler = MyHandler(this)

    private var bluetoothService: BluetoothConnectionService? = null
    private var isServiceBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            val binder = service as BluetoothConnectionService.LocalBinder
            bluetoothService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothConnectionService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (isServiceBound) {
            bluetoothService?.connect(device)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, BluetoothFragment.newInstance())
                .commitNow()
        }

        if (!hasAllPermissions(this)) {
            grantPermissions()
        }

        bluetoothCommunicationService = BluetoothCommunicationService(handler)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        deviceViewModel = ViewModelProvider(this)[BluetoothDeviceViewModel::class.java]


        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondReceiver, filter)

        val discoverFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverFilter)


        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        bluetoothAdapter.startDiscovery()

//        button.setOnClickListener {
//            val message = sendTextView.text.toString()
//            bluetoothCommunicationService.write(message.toByteArray())
//        }
    }


    private val discoverReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    Log.d(TAG, "discoverReceiver: found $device")
                    deviceViewModel.addDevice(device)
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
                        BluetoothDevice.BOND_BONDED -> {
                            Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
//                            switchToFragment()
                        }

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

    private fun switchToFragment() {
        val newFragment = MeasurementFragment()
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.fragment_container, newFragment)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(bondReceiver)
        unregisterReceiver(discoverReceiver)
        bluetoothService?.disconnect()
        bluetoothCommunicationService.stopConnection()
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
        const val TAG: String = "BluetoothActivity"
    }
}

