package com.agrosense.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.bluetooth.BluetoothClient
import com.agrosense.app.bluetooth.BluetoothState
import com.google.android.material.switchmaterial.SwitchMaterial

class BluetoothActivity : AppCompatActivity() {

    private val bluetoothClient : BluetoothClient = BluetoothClient.getInstance()
    private val bluetoothState =  BluetoothState()
    private lateinit var bluetoothAdapter : BluetoothAdapter
    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        recyclerView  = findViewById(R.id.devices)

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondReceiver(this), filter)

        val btSwitch = findViewById<SwitchMaterial>(R.id.btnONOFF)
        val discover = findViewById<Button>(R.id.btnFindUnpairedDevices)

        val button = findViewById<Button>(R.id.send)
        val sendTextView = findViewById<EditText>(R.id.send_message)

        btSwitch.setOnCheckedChangeListener{_, isChecked ->
            if(isChecked)
                bluetoothState.enableBluetooth(this, toggleBluetoothReceiver)
            else
                bluetoothState.disableBluetooth(this, toggleBluetoothReceiver)
        }
        discover.setOnClickListener{
            bluetoothState.toggleDiscover(this, discoverDevices(applicationContext))
        }

        button.setOnClickListener{
            val message = sendTextView.text.toString()
            bluetoothClient.sendRequest(message)
        }

        refreshAdapter()
    }

    private fun refreshAdapter(){
        recyclerView.adapter = DeviceAdapter(devices){device ->
            run {
                connect(device)
            }
        }

    }

    private fun connect(device: BluetoothDevice){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onItemClick: PERMISSION DENIED.")
        }
        bluetoothAdapter.cancelDiscovery()
        Log.d(TAG, "onItemClick: Trying to pair with " + device.name)
        device.createBond()

        bluetoothClient.connect(bluetoothAdapter.getRemoteDevice(device.address), this)
        Log.d(TAG, "onItemClick: connected to " + device.name + ", " +device.address)

    }

    private fun discoverDevices(rootContext: Context) = object: BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context?, intent: Intent?) {

            when(intent?.action){
                BluetoothDevice.ACTION_FOUND ->{
                    Log.d(TAG, "discoverDevices: ACTION FOUND.")
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    devices + device

                    if (ActivityCompat.checkSelfPermission(rootContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    if(device != null)
                        Log.d(TAG, "discoverDevices: Found device:" + device.name + " " + device.address)

                    Log.d(TAG, "discoverDevices: device list: $devices")
                    refreshAdapter()

                }

            }
        }

    }

    private fun bondReceiver(rootContext: Context) = object: BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ActivityCompat.checkSelfPermission(rootContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onReceive: PERMISSION DENIED.")
                return
            }
            when(intent?.action){
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    when(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)?.bondState){
                        BluetoothDevice.BOND_BONDED -> Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                        BluetoothDevice.BOND_BONDING -> Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                        BluetoothDevice.BOND_NONE -> Log.d(TAG, "BroadcastReceiver: BOND_NONE.")
                    }

                }
            }
        }

    }


    companion object{
        private const val TAG: String = "BluetoothActivity"
        private val devices: List<BluetoothDevice> =  ArrayList()
        private  val toggleBluetoothReceiver =  object : BroadcastReceiver(){

            override fun onReceive(context: Context?, intent: Intent?) {

                if (intent != null)
                    if(intent.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){

                        when(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)){
                            BluetoothAdapter.STATE_OFF -> {
                                Log.d(TAG, "toggleBluetoothReceiver: STATE OFF")
                            }
                            BluetoothAdapter.STATE_TURNING_OFF -> {
                                Log.d(TAG, "toggleBluetoothReceiver: STATE TURNING OFF")
                            }
                            BluetoothAdapter.STATE_ON -> {
                                Log.d(TAG, "toggleBluetoothReceiver: STATE TURNING OFF")
                            }
                            BluetoothAdapter.STATE_TURNING_ON -> {
                                Log.d(TAG, "toggleBluetoothReceiver: STATE TURNING ON")
                            }
                        }

                    }
            }
        }

    }
}

