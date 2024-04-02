package com.agrosense.app.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.joda.time.DateTime

@Entity(tableName = "measurement")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val measurementId: Long?,
    val name: String,
    val start: DateTime,
    val end: DateTime?,
    val maxValue: Double,
    val minValue: Double,
)

@Entity(tableName = "reading")
data class TemperatureReading(
    @PrimaryKey(autoGenerate = true) val temperatureReadingId: Long?,
    val value: Double,
    @ColumnInfo(name = "recordedAt") val recordedAt: DateTime,
    val measurementParentId: Long
)

data class MeasurementWithReadings(
    @Embedded val measurement: Measurement,
    @Relation(
        parentColumn = "measurementId",
        entityColumn = "measurementParentId"
    ) val readings: List<TemperatureReading>
)
