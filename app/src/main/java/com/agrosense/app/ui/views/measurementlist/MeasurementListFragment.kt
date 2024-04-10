package com.agrosense.app.ui.views.measurementlist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agrosense.app.R

class MeasurementListFragment : Fragment() {

    companion object {
        fun newInstance() = MeasurementListFragment()
    }

    private lateinit var viewModel: MeasurementListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_measurement_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MeasurementListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}