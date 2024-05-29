package com.agrosense.app.viewmodelfactory

import androidx.lifecycle.ViewModel
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.ui.views.linechart.LineChartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class LineChartViewModelFactoryTest{

    private var measurementDao: MeasurementDao = Mockito.mock(MeasurementDao::class.java)

    private lateinit var factory: LineChartViewModelFactory

    @Before
    fun setUp() {
        factory = LineChartViewModelFactory(measurementDao)
    }

    @Test
    fun `should create class of LineChartViewModel type`() {
        val viewModel = factory.create(LineChartViewModel::class.java)
        assertEquals(LineChartViewModel::class.java,viewModel::class.java )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception of wrong class was provided`() {
        class DummyViewModel : ViewModel()
        factory.create(DummyViewModel::class.java)
        fail("Should have thrown IllegalArgumentException for unknown ViewModel class")
    }
}