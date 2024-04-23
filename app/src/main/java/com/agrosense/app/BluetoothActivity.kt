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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.dsl.db.AgroSenseDatabase.Companion.getDatabase
import com.agrosense.app.rds.MessageHandler
import com.agrosense.app.rds.bluetooth.BluetoothCommunicationService
import com.agrosense.app.rds.bluetooth.BluetoothConnectionService
import com.agrosense.app.ui.views.devices.BluetoothDeviceViewModel
import com.agrosense.app.ui.views.main.NavFragment
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
    private lateinit var bluetoothCommunicationService: BluetoothCommunicationService

    private lateinit var deviceViewModel: BluetoothDeviceViewModel

    private lateinit var handler: Handler

    private var bluetoothService: BluetoothConnectionService? = null
    private var isServiceBound: Boolean = false


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        getDatabase(this)
        handler = MessageHandler(this)
        if (savedInstanceState == null) {
            replaceFragment(NavFragment.newInstance())
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
            startActivityIfNeeded(enableBtIntent, REQUEST_ENABLE_BT)
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
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val bondState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    when (bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                            if (isServiceBound) {
                                if (device != null) {
                                    bluetoothService?.connect(device.createRfcommSocketToServiceRecord(
                                        MY_UUID))
                                }
                            }
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

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        device.createBond()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    fun getCommunicationService(): BluetoothCommunicationService {
        return bluetoothCommunicationService
    }


    companion object {
        const val TAG: String = "BluetoothActivity"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}

