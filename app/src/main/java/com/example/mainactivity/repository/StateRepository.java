package com.example.mainactivity.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;


import androidx.lifecycle.LiveData;

import com.example.mainactivity.db.dao.StateDao;
import com.example.mainactivity.db.database.AppDB;
import com.example.mainactivity.interfaces.StatesInterface;
import com.example.mainactivity.source.StatesData;

import java.util.List;

public class StateRepository {
    private StateDao stateDao;

    public void insertStates(final StatesInterface statesInterface, final List<StatesData> statesData, final Context context) {
        AppDB appDataBase = AppDB.getDatabase(context);
        if (appDataBase != null) {
            stateDao = appDataBase.stateDao();
        }

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
            stateDao.insertState(statesData);
            return stateDao.stateCount();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            statesInterface.stateCount(integer);
        }
    }

    public void updateFav(final StatesInterface statesInterface,
                          String name,
                          Boolean flag,
                          final Context context) {
        AppDB appDataBase = AppDB.getDatabase(context);
        if (appDataBase != null) {
            stateDao = appDataBase.stateDao();
        }

        new UpdateTask(statesInterface, name, flag).execute();
    }


    public void resetFav(Context context, StatesInterface statesInterface) {

        AppDB appDataBase = AppDB.getDatabase(context);
        if (appDataBase != null) {
            stateDao = appDataBase.stateDao();
            new ResetTask(context, statesInterface).execute();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class DeleteTask extends AsyncTask<Void, Void, Integer> {
        String name;
        StatesInterface statesInterface;

        DeleteTask(StatesInterface statesInterface,
                   String name) {
            this.name = name;
            this.statesInterface = statesInterface;
        }


        @Override
        protected Integer doInBackground(Void... voids) {
            return stateDao.deleteItem(name);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            statesInterface.stateCount(integer);
        }
    }

    public void deleteItem(final StatesInterface statesInterface,
                           String name,
                           final Context context) {
        AppDB appDataBase = AppDB.getDatabase(context);
        if (appDataBase != null) {
            stateDao = appDataBase.stateDao();
        }

        new DeleteTask(statesInterface, name).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ResetTask extends AsyncTask<Void, Void, Integer> {
        StatesInterface statesInterface;
        Context context;

        ResetTask(Context context, StatesInterface statesInterface) {
            this.statesInterface = statesInterface;
            this.context = context;
        }


        @Override
        protected Integer doInBackground(Void... voids) {
            stateDao.resetFav();
            return stateDao.stateCount();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            statesInterface.stateCount(integer);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateTask extends AsyncTask<Void, Void, Integer> {
        String name;
        Boolean flag;
        StatesInterface statesInterface;

        UpdateTask(StatesInterface statesInterface,
                   String name, Boolean flag) {
            this.name = name;
            this.flag = flag;
            this.statesInterface = statesInterface;
        }


        @Override
        protected Integer doInBackground(Void... voids) {
            stateDao.updateFav(flag, name);
            return stateDao.stateCount();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            statesInterface.stateCount(integer);
        }
    }


    public LiveData<StatesData> checkFav(
            String name,
            final Context context) {

        AppDB appDataBase = AppDB.getDatabase(context);
        if (appDataBase != null) {
            stateDao = appDataBase.stateDao();
        }

        return stateDao.checkCity(name);
    }
}
