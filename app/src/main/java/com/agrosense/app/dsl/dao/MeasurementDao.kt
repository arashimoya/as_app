package com.agrosense.app.dsl.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.MeasurementWithReadings
import com.agrosense.app.domain.entity.TemperatureReading
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertMeasurements(vararg measurements: Measurement)

    @Update
    suspend fun updateMeasurements(vararg measurements: Measurement)

    @Delete
    suspend fun deleteMeasurements(vararg measurements: Measurement)

    @Transaction
    @Query("SELECT * FROM measurement")
    fun getMeasurementsWithReadings(): List<MeasurementWithReadings>


    @Query("SELECT * from reading WHERE measurementParentId = :measurementId")
    fun loadReadingsByMeasurement(measurementId: Long): Flow<List<TemperatureReading>>
}