package com.agrosense.app.ui.views.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.agrosense.app.R
import com.agrosense.app.ui.pager.ViewPagerAdapter

class NavFragment : Fragment() {

    private lateinit var viewPager: ViewPager

    companion object {
        fun newInstance() = NavFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nav, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        refreshViewPager()

        return view
    }

    override fun onResume() {
        super.onResume()
        //refreshViewPager()
    }

    private fun refreshViewPager() {
        val newAdapter = ViewPagerAdapter.newInstance(requireActivity().supportFragmentManager)
        viewPager.adapter = newAdapter
    }




}