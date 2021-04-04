package com.example.mainactivity.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mainactivity.db.dao.StateDao
import com.example.mainactivity.source.StatesData

@Database(entities = [StatesData::class], version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun stateDao(): StateDao?

    companion object {
        private var INSTANCE: AppDB? = null
        fun getDatabase(context: Context): AppDB? {
            if (INSTANCE == null) {
                    if (INSTANCE == null) {
                        INSTANCE =
                            Room.databaseBuilder(
                                context.applicationContext,
                                AppDB::class.java, "Mobi.db")
                                .fallbackToDestructiveMigration()
                                .build()
                    }

            }
            return INSTANCE
        }
    }
}