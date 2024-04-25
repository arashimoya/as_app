package com.agrosense.app.dsl

import com.agrosense.app.domain.entity.Measurement

interface MeasurementRepo {

    fun insertMeasurement(measurement: Measurement)
    fun updateEndForAllMeasurements()
    fun insertNewMeasurement(measurement: Measurement)
}