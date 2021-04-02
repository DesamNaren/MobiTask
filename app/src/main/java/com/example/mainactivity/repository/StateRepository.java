package com.example.mainactivity.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;


import com.example.mainactivity.db.dao.StateDao;
import com.example.mainactivity.db.database.AppDB;
import com.example.mainactivity.db.database.AppDataBase;
import com.example.mainactivity.interfaces.StatesInterface;
import com.example.mainactivity.source.StatesData;

import java.util.List;

public class StateRepository {
    private StateDao stateDao;
    public void insertStates(final StatesInterface statesInterface, final List<StatesData> statesData, final Context context) {
        AppDB appDataBase = AppDataBase.Companion.getDatabase(context);
        stateDao = appDataBase.stateDao();

        new InsertStatesTask(statesInterface, statesData).execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class InsertStatesTask extends AsyncTask<Void, Void, Integer> {
        List<StatesData> statesData;
        StatesInterface statesInterface;

        InsertStatesTask(StatesInterface statesInterface,
                         List<StatesData> statesData) {
            this.statesData = statesData;
            this.statesInterface = statesInterface;
        }


        @Override
        protected Integer doInBackground(Void... voids) {
            stateDao.deleteStates();
            stateDao.insertState(statesData);
            return stateDao.stateCount();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            statesInterface.stateCount(integer);
        }
    }
}
