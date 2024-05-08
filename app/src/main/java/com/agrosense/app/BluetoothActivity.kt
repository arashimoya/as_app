package com.agrosense.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.db.AgroSenseDatabase.Companion.getDatabase
import com.agrosense.app.monitoring.ThresholdChecker
import com.agrosense.app.monitoring.ThresholdExceedanceHandler
import com.agrosense.app.monitoring.ThresholdNotifier
import com.agrosense.app.permission.IPermissionHelper
import com.agrosense.app.permission.PermissionHelper
import com.agrosense.app.rds.MessageHandler
import com.agrosense.app.rds.bluetooth.BluetoothCommunicationService
import com.agrosense.app.rds.bluetooth.BluetoothConnectionService
import com.agrosense.app.ui.views.devices.BluetoothDeviceViewModel
import com.agrosense.app.ui.views.main.NavFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.joda.time.DateTime
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class BluetoothActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothCommunicationService: BluetoothCommunicationService
    private lateinit var deviceViewModel: BluetoothDeviceViewModel
    private lateinit var handler: Handler
    private var bluetoothService: BluetoothConnectionService? = null
    private var permissionHelper: IPermissionHelper = PermissionHelper()

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

        permissionHelper.grantBLEPermissions(this)
        permissionHelper.grantNotificationPermissions(this)

        createNotificationChannel()

        bluetoothCommunicationService = BluetoothCommunicationService(handler)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        deviceViewModel = ViewModelProvider(this)[BluetoothDeviceViewModel::class.java]


        registerReceivers()


        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityIfNeeded(enableBtIntent, REQUEST_ENABLE_BT)
        }

        bluetoothAdapter.startDiscovery()

        //testing
//        testNotifications()
    }

    private fun registerReceivers() {
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondReceiver, filter)

        val discoverFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverFilter)
    }

    private fun testNotifications() {

        val timer = Timer()
        val notifier = ThresholdNotifier()
        notifier.addListener(ThresholdExceedanceHandler(this))
        val checker = ThresholdChecker(notifier)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val temperatureReading =
                    TemperatureReading(null, getCurrentTemperature(), DateTime.now(), 1)
                val ms = Measurement(1, "test", DateTime(1714072430), null, 25.0, 20.0)
                Log.i(
                    TAG,
                    "reading: ${temperatureReading.value}, max: ${ms.maxValue}, min: ${ms.minValue}"
                )
                checker.check(
                    listOf(temperatureReading),
                    ms
                )
            }
        }, 0, 5000)
    }

    private fun createNotificationChannel() {
        val channelId = "THRESHOLD"
        val channelName = "Threshold"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Did a reading exceed the threshold?"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getCurrentTemperature(): Double {
        return Math.random() * 100
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
                                    bluetoothService?.connect(
                                        device.createInsecureRfcommSocketToServiceRecord(
                                            MY_UUID
                                        )
                                    )
                                    if(bluetoothService?.isConnected() == true){
                                        findViewById<FloatingActionButton>(R.id.fab_add_measurement).isEnabled = true
                                    }
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

