package com.agrosense.app.ui.views.main

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.ui.adapter.DeviceAdapter
import com.agrosense.app.R

class BluetoothFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: BluetoothDeviceViewModel

    companion object {
        fun newInstance() = BluetoothFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[BluetoothDeviceViewModel::class.java]



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.devices)
        recyclerView.adapter = DeviceAdapter(listOf(), ::connect)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.devices.observe(viewLifecycleOwner) {
            refreshAdapter(it)
        }

    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        Log.d(BluetoothActivity.TAG, "connect: Trying to pair with " + device.name)
        if (activity is BluetoothActivity) {
            (activity as BluetoothActivity).connectToDevice(device)
        }
    }

    private fun refreshAdapter(newList: List<BluetoothDevice>) {
        val adapter = recyclerView.adapter as DeviceAdapter
        adapter.updateDataSet(newList)
    }



}