package com.agrosense.app.dsl.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agrosense.app.domain.entity.Measurement
import com.agrosense.app.domain.entity.TemperatureReading
import com.agrosense.app.dsl.converter.Converters
import com.agrosense.app.dsl.dao.MeasurementDao
import com.agrosense.app.dsl.dao.ReadingDao

@Database(entities = [Measurement::class, TemperatureReading::class], version = 3)
@TypeConverters(Converters::class)
abstract class AgroSenseDatabase : RoomDatabase(){
    abstract fun measurementDao(): MeasurementDao
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AgroSenseDatabase? = null

        fun getDatabase(context: Context): AgroSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgroSenseDatabase::class.java,
                    "agrosense_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}