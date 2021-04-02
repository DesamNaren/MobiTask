package com.example.mainactivity.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mainactivity.db.dao.StateDao
import com.example.mainactivity.source.StatesData

@Database(entities = [StatesData::class], version = 2, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun stateDao(): StateDao?

    companion object {
        private var INSTANCE: AppDB? = null
        fun getDatabase(context: Context): AppDB? {
            if (INSTANCE == null) {
                synchronized(AppDB::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDB::class.java, "Virtuo.db"
                        ) // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this codelab.
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
