package com.agrosense.app.dsl.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.agrosense.app.domain.entity.TemperatureReading

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureReadings(vararg readings: TemperatureReading)

    @Update
    suspend fun updateTemperatureReadings(vararg readings: TemperatureReading)

    @Delete
    suspend fun deleteTemperatureReadings(vararg readings: TemperatureReading)
}