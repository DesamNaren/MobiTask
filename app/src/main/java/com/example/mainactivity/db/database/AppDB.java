package com.example.mainactivity.db.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.mainactivity.db.dao.StateDao;
import com.example.mainactivity.source.StatesData;


/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */

@Database(entities = {StatesData.class},
        version = 2, exportSchema = false)

public abstract class AppDB extends RoomDatabase {

    public abstract StateDao stateDao();
    private static AppDB INSTANCE;


    public static AppDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDB.class, "Virtuo.db")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this codelab.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
