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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.rds.MessageHandler
import com.agrosense.app.rds.bluetooth.BluetoothCommunicationService
import com.agrosense.app.rds.bluetooth.BluetoothConnectionService
import com.agrosense.app.ui.views.main.BluetoothDeviceViewModel
import com.agrosense.app.ui.views.main.BluetoothFragment
import com.agrosense.app.ui.views.measurementlist.MeasurementListFragment


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

    private val handler: Handler = MessageHandler(this)

    private var bluetoothService: BluetoothConnectionService? = null
    private var isServiceBound: Boolean = false


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MeasurementListFragment.newInstance())
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


    override fun onBackPressed() {
        if (!navigateBack()) {
            super.onBackPressed()
        }
    }
    private fun navigateBack(): Boolean {
        val fragmentManager = supportFragmentManager
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }



    companion object {
        const val TAG: String = "BluetoothActivity"
    }
}

