package com.agrosense.app.ui.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.agrosense.app.ui.views.devices.BluetoothFragment
import com.agrosense.app.ui.views.measurementlist.MeasurementListFragment

class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> MeasurementListFragment.newInstance()
            1 -> BluetoothFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }

    }

    companion object {
        fun newInstance(fragmentManager: FragmentManager): ViewPagerAdapter = ViewPagerAdapter(fragmentManager)
    }

}