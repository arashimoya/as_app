package com.agrosense.app.dsl.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

@Dao
interface MeasurementDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertMeasurements(vararg measurements: Measurement): LongArray

    @Update
    suspend fun updateMeasurements(vararg measurements: Measurement)

    @Delete
    suspend fun deleteMeasurements(vararg measurements: Measurement)

    @Query("SELECT * FROM measurement ORDER BY `end`")
    fun loadMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * from reading WHERE measurementParentId = :measurementId")
    fun loadReadingsByMeasurement(measurementId: Long): Flow<List<TemperatureReading>>

    @Query("SELECT * from reading order by recordedAt DESC LIMIT 1")
    fun loadLastReadingForMeasurement(): Flow<TemperatureReading?>

    @Query("SELECT * from measurement where `end` = null LIMIT 1")
    fun loadLastNotFinishedMeasurement(): Measurement?

    @Query("UPDATE measurement SET `end` = :dateTime WHERE `end` is null")
    suspend fun updateAllMeasurementEndsToNow(dateTime: DateTime)

}