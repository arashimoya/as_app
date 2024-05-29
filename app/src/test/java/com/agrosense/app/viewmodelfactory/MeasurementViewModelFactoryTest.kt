package com.agrosense.app.viewmodelfactory

import androidx.lifecycle.ViewModel
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.ui.views.measurement.MeasurementViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


class MeasurementViewModelFactoryTest{

    private var measurementDao: MeasurementDao = mock(MeasurementDao::class.java)

    private lateinit var factory: MeasurementViewModelFactory

    @Before
    fun setUp() {
        factory = MeasurementViewModelFactory(measurementDao)
    }

    @Test
    fun `should create class of MeasurementViewModel type`() {
        val viewModel = factory.create(MeasurementViewModel::class.java)
        assertEquals(MeasurementViewModel::class.java,viewModel::class.java )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create - throws IllegalArgumentException for unknown ViewModel class`() {
        class DummyViewModel : ViewModel()
        factory.create(DummyViewModel::class.java)
        fail("Should have thrown IllegalArgumentException for unknown ViewModel class")
    }
}