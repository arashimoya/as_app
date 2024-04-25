package com.agrosense.app.dsl.converter

import androidx.room.TypeConverter
import org.joda.time.DateTime

class Converters {

    @TypeConverter
    fun fromISO8601(value: String?): DateTime? {
        if (value == null || value == "null")
            return null
        return DateTime.parse(value)
    }

    @TypeConverter
    fun fromDateTime(dateTime: DateTime?): String? {
        if (dateTime == null)
            return null
        return dateTime.toString()
    }
}