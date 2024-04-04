package com.agrosense.app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(
    var data: List<BluetoothDevice>,
    private val onItemClick: (BluetoothDevice)-> Unit)
    : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View, onItemClicked: (Int)-> Unit) : RecyclerView.ViewHolder(view) {


        private val deviceNameText: TextView = view.findViewById(R.id.device_id)
        init {
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }

        fun bind(model: BluetoothDevice) {
            deviceNameText.text = model.name ?: "Unknown Device"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return ViewHolder(viewHolder) {
            onItemClick(data[it])
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(data[position])
        if (ActivityCompat.checkSelfPermission(viewHolder.itemView.context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            Log.d("DeviceAdapter", "onBindViewHolder: " + data[position].name)
        }

    }

    fun updateDataSet(devices: List<BluetoothDevice>){
        val oldData = data
        data + devices
        notifyItemRangeInserted(oldData.size-1, devices.size)
    }


}