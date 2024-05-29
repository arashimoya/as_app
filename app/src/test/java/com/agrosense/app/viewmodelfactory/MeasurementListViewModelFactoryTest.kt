package com.agrosense.app.viewmodelfactory

import androidx.lifecycle.ViewModel
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.ui.views.measurementlist.MeasurementListViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MeasurementListViewModelFactoryTest{

    private var measurementDao: MeasurementDao = Mockito.mock(MeasurementDao::class.java)

    private lateinit var factory: MeasurementListViewModelFactory

    @Before
    fun setUp() {
        factory = MeasurementListViewModelFactory(measurementDao)
    }

    @Test
    fun `should create class of MeasurementListViewModel type`() {
        val viewModel = factory.create(MeasurementListViewModel::class.java)
        assertEquals(MeasurementListViewModel::class.java,viewModel::class.java )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception of wrong class was provided`() {
        class DummyViewModel : ViewModel()
        factory.create(DummyViewModel::class.java)
        fail("Should have thrown IllegalArgumentException for unknown ViewModel class")
    }
}