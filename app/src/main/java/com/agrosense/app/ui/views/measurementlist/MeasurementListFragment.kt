package com.agrosense.app.ui.views.measurementlist

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.R
import com.agrosense.app.datautil.resolver.measurement.MeasurementDataSetResolver
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.MeasurementRepo
import com.agrosense.app.dsl.MeasurementRepository
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.timeprovider.CurrentTimeProvider
import com.agrosense.app.timeprovider.TimeProvider
import com.agrosense.app.ui.adapter.MeasurementsAdapter
import com.agrosense.app.ui.views.measurement.MeasurementFragment
import com.agrosense.app.viewmodelfactory.MeasurementListViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MeasurementListFragment : Fragment() {

    private lateinit var measurementListViewModel: MeasurementListViewModel
    private lateinit var timeProvider: TimeProvider
    private lateinit var measurementRepository: MeasurementRepo

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var floorText: EditText
    private lateinit var ceilingText: EditText
    private lateinit var nameText: EditText

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
        timeProvider = CurrentTimeProvider()
        measurementRepository = MeasurementRepository.getInstance(requireContext(), timeProvider)
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
        recyclerView.adapter = MeasurementsAdapter(mutableListOf(), ::openDetailedView, MeasurementDataSetResolver())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fab = view.findViewById(R.id.fab_add_measurement)
        fab.setOnClickListener {
            openCreateDialog()
        }
    }

    private fun openCreateDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_insert_new_measurement, null)

        builder.setView(dialogView)
            .setTitle("Start new Measurement")
            .setPositiveButton("Confirm") { dialog, _ ->
                handleConfirm(dialogView, dialog)

            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        builder.create().show()
    }

    private fun handleConfirm(dialogView: View, dialog: DialogInterface) {
        floorText = dialogView.findViewById(R.id.edit_floor)
        ceilingText = dialogView.findViewById(R.id.edit_ceiling)
        nameText = dialogView.findViewById(R.id.edit_name)

        val floor = floorText.text.toString().toDouble()
        val ceiling = ceilingText.text.toString().toDouble()
        val name = nameText.text.toString()

        if (isInputValid(name, floor, ceiling)) {
            proceedWithNewMeasurement(name, ceiling, floor)
            dialog.dismiss()
            openNotFinishedView()
        }
    }
//TODO
    private fun isInputValid(name: String, floor: Double, ceiling: Double): Boolean {
        return if (floor > ceiling) {
            floorText.error = "Floor cannot be greater than ceiling!"
            false
        } else if (name.isEmpty()) {
            nameText.error = "Name cannot be empty!"
            false
        } else {
            true
        }
    }


    private fun proceedWithNewMeasurement(name: String, ceiling: Double, floor: Double) {
        //TODO call bluetooth
        measurementRepository.insertNewMeasurement(
            Measurement(
                name,
                timeProvider.now(),
                ceiling,
                floor
            )
        )
    }


    private fun openDetailedView(measurement: Measurement) {


        //TODO switch to graphs when measurement is historical (end != null)
        if (measurement.end == null) {
            openNotFinishedView()
        } else {
//            val args = Bundle()
//            measurement.measurementId?.let { args.putLong(measurementKey, it) }
//            fragment.arguments = args
            Toast.makeText(requireContext(), "Not implemented yet! ^^", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openNotFinishedView() {
        (requireActivity() as BluetoothActivity).replaceFragment(MeasurementFragment.newInstance())
    }

}