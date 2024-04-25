package com.agrosense.app.datautil.validator

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText

class NewMeasurementDataValidator {

    fun validate(floorText: EditText, ceilingText: EditText, nameText: EditText, confirmButton: Button) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nameNotEmpty = nameText.text.isNotEmpty() && nameText.text.toString() != ""
                val floorEmpty = floorText.text.isEmpty()
                val ceilingEmpty = ceilingText.text.isEmpty()
                val floor = if (!floorEmpty) floorText.text.toString().toDoubleOrNull() else null
                val ceiling = if (!ceilingEmpty) ceilingText.text.toString().toDoubleOrNull() else null


                val isValid = nameNotEmpty && (floorEmpty || ceilingEmpty || (floor!! < ceiling!!))
                confirmButton.isEnabled = isValid
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        nameText.addTextChangedListener(textWatcher)
        floorText.addTextChangedListener(textWatcher)
        ceilingText.addTextChangedListener(textWatcher)
    }
}

