package com.agrosense.app.ui.views.devices

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


class BluetoothDeviceViewModelTest{

    inline fun <reified T> mock(): T = mock(T::class.java)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BluetoothDeviceViewModel

    private var observer: Observer<List<BluetoothDevice>> = mock()

    @Before
    fun setUp() {
        viewModel = BluetoothDeviceViewModel()
        viewModel.devices.observeForever(observer)
    }

    @Test
    fun `should return zero devices`() {
        verify(observer).onChanged(emptyList())
    }

    @Test
    fun `return a single device devices list`() {
        viewModel.addDevice(device1)
        verify(observer).onChanged(listOf(device1))
    }

    @Test
    fun `should add multiple unique devices and retrieve the two`() {
        viewModel.addDevice(device1)
        viewModel.addDevice(device2)
        verify(observer).onChanged(listOf(device1, device2))
    }

    @Test
    fun `should not add duplicate device to the list`() {
        viewModel.addDevice(device1)
        viewModel.addDevice(device1)
        verify(observer).onChanged(listOf(device1))
    }

    companion object{
        private val device1 = mock(BluetoothDevice::class.java)
        private val device2 = mock(BluetoothDevice::class.java)
    }
}