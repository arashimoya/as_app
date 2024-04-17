package com.agrosense.app.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(tableName = "measurement")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val measurementId: Long?,
    val name: String,
    val start: DateTime,
    var end: DateTime?,
    val maxValue: Double,
    val minValue: Double,
){
    constructor(
        name: String,
        start: DateTime,
        maxValue: Double,
        minValue: Double
    ): this(null, name, start, null, maxValue, minValue)
}

@Entity(tableName = "reading")
data class TemperatureReading(
    @PrimaryKey(autoGenerate = true) val temperatureReadingId: Long?,
    val value: Double,
    @ColumnInfo(name = "recordedAt") val recordedAt: DateTime,
    val measurementParentId: Long
)

