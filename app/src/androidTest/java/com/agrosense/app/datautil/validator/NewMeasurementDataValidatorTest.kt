package com.agrosense.app.datautil.validator

import android.text.Editable
import android.text.SpannableStringBuilder
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("TODO fix it idk")
class NewMeasurementDataValidatorTest{

    private val validator = NewMeasurementDataValidator()
    private val nameET = EditText(ApplicationProvider.getApplicationContext())
    private val floorET = EditText(ApplicationProvider.getApplicationContext())
    private val ceilingET = EditText(ApplicationProvider.getApplicationContext())
    private val confirmButton = Button(ApplicationProvider.getApplicationContext())


    @Test
    fun should_not_enable_button_for_empty_name(){


        validator.validate(floorET, ceilingET, nameET, confirmButton)

        nameET.text = "".toEditable()

        assertFalse(confirmButton.isEnabled)
    }

    @Test
    fun should_not_enable_button_for_empty_name_even_with_correct_threshold_values(){


        validator.validate(floorET, ceilingET, nameET, confirmButton)

        nameET.text = "".toEditable()
        floorET.text = 20.0.toEditable()
        ceilingET.text = 30.0.toEditable()

        assertFalse(confirmButton.isEnabled)
    }

    @Test
    fun should_not_enable_button_for_incorrect_threshold_values() {


        validator.validate(floorET, ceilingET, nameET, confirmButton)

        nameET.text = "str".toEditable()
        floorET.text = 50.0.toEditable()
        ceilingET.text = 30.0.toEditable()

        assertFalse(confirmButton.isEnabled)
    }

    @Test
    fun should_enable_button_for_all_correct_values() {
        validator.validate(floorET, ceilingET, nameET, confirmButton)

        nameET.text = "str".toEditable()
        floorET.text = 20.0.toEditable()
        ceilingET.text = 30.0.toEditable()

        assert(confirmButton.isEnabled)
    }

    private fun Double.toEditable(): Editable {
        return SpannableStringBuilder(this.toString())
    }

    private fun String.toEditable(): Editable {
        return SpannableStringBuilder(this)
    }
}