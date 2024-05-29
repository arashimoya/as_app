package com.agrosense.app.ui.views.dialog.insertmeasurement

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.R
import com.agrosense.app.datautil.validator.NewMeasurementDataValidator
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.dsl.MeasurementRepo
import com.agrosense.app.dsl.MeasurementRepository
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.rds.bluetooth.MeasurementManager
import com.agrosense.app.timeprovider.CurrentTimeProvider
import com.agrosense.app.timeprovider.TimeProvider
import com.agrosense.app.ui.views.measurement.MeasurementFragment

class InsertNewMeasurementDialog : DialogFragment() {

    private lateinit var floorText: EditText
    private lateinit var ceilingText: EditText
    private lateinit var nameText: EditText

    private val inputValidator = NewMeasurementDataValidator()
    private lateinit var timeProvider: TimeProvider
    private lateinit var measurementRepository: MeasurementRepo
    private lateinit var measurementManager: MeasurementManager


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            timeProvider = CurrentTimeProvider()
            measurementRepository = MeasurementRepository.getInstance(
                AgroSenseDatabase.getDatabase(requireContext()).measurementDao(), timeProvider
            )
            measurementManager =
                MeasurementManager((requireActivity() as BluetoothActivity).getCommunicationService())
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater

            val dialogView =
                inflater.inflate(R.layout.dialog_insert_new_measurement, null)
            floorText = dialogView.findViewById(R.id.edit_floor)
            ceilingText = dialogView.findViewById(R.id.edit_ceiling)
            nameText = dialogView.findViewById(R.id.edit_name)

            builder.setView(dialogView)
                .setTitle("Start new Measurement")
                .setPositiveButton("Confirm") { dialog, _ ->
                    handleConfirm(dialog)

                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
            val confirmButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            confirmButton.isEnabled = false
            inputValidator.validate(floorText, ceilingText, nameText, confirmButton)
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun handleConfirm(dialog: DialogInterface) {
        val floor = floorText.text.toString().toDoubleOrNull()
        val ceiling = ceilingText.text.toString().toDoubleOrNull()
        val name = nameText.text.toString()

        proceedWithNewMeasurement(name, ceiling, floor)
        dialog.dismiss()
        openNotFinishedView()

    }

    private fun proceedWithNewMeasurement(name: String, ceiling: Double?, floor: Double?) {
        measurementManager.start()
        measurementRepository.insertNewMeasurement(
            Measurement(
                name,
                timeProvider.now(),
                ceiling,
                floor
            )
        )
    }

    private fun openNotFinishedView() {
        (requireActivity() as BluetoothActivity).replaceFragment(MeasurementFragment.newInstance())
    }
}
