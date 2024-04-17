package com.agrosense.app.ui.pager

import androidx.fragment.app.FragmentManager
import com.agrosense.app.ui.views.devices.BluetoothFragment
import com.agrosense.app.ui.views.measurementlist.MeasurementListFragment
import org.junit.jupiter.api.Assertions.*

import org.junit.Test
import org.mockito.Mockito.mock

 class ViewPagerAdapterTest {

    private val fragmentManager = mock(FragmentManager::class.java)
    private val viewPagerAdapter = ViewPagerAdapter(fragmentManager)

    @Test
    fun getCount() {
        assertEquals(2, viewPagerAdapter.count)
    }

    @Test
    fun `getItem for position 0 should return MeasurementListFragment`() {
        assertEquals(MeasurementListFragment()::class.java, viewPagerAdapter.getItem(0)::class.java)
    }

    @Test
    fun `getItem for position 1 should return BluetoothFragment`() {
        assertEquals(BluetoothFragment()::class.java, viewPagerAdapter.getItem(1)::class.java)
    }

    @Test
    fun `getItem for other position should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) { viewPagerAdapter.getItem(2) }
        assertThrows(IllegalArgumentException::class.java) { viewPagerAdapter.getItem(-1) }
        assertThrows(IllegalArgumentException::class.java) { viewPagerAdapter.getItem(-1787) }
    }
}