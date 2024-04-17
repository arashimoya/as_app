package com.agrosense.app.ui.views.measurementlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.R
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.ui.adapter.MeasurementsAdapter
import com.agrosense.app.ui.views.measurement.MeasurementFragment
import com.agrosense.app.viewmodelfactory.MeasurementListViewModelFactory
import kotlinx.coroutines.launch

class MeasurementListFragment : Fragment() {
    private lateinit var measurementListViewModel: MeasurementListViewModel

    private lateinit var recyclerView: RecyclerView

    companion object {
        fun newInstance() = MeasurementListFragment()
        val measurementKey: String = "measurementId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        measurementListViewModel =
            ViewModelProvider(
                requireActivity(),
                MeasurementListViewModelFactory(
                    AgroSenseDatabase.getDatabase(requireContext()).measurementDao()
                )
            )[MeasurementListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLifecycleOwner.lifecycleScope.launch {
            measurementListViewModel.measurements.collect { measurements ->
                (recyclerView.adapter as MeasurementsAdapter).updateDataSet(measurements)
            }
        }
        return inflater.inflate(R.layout.fragment_measurement_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.measurements)
        recyclerView.adapter = MeasurementsAdapter(mutableListOf(), ::openDetailedView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun openDetailedView(measurement: Measurement) {


        //TODO switch to graphs when measurement is historical (end != null)
        if (measurement.end == null) {
            (requireActivity() as BluetoothActivity).replaceFragment(MeasurementFragment.newInstance())
        } else {
//            val args = Bundle()
//            measurement.measurementId?.let { args.putLong(measurementKey, it) }
//            fragment.arguments = args
            Toast.makeText(requireContext(), "Not implemented yet! ^^", Toast.LENGTH_SHORT).show()
        }
    }

}