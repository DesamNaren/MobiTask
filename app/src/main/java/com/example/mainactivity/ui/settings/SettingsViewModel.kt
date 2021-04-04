package com.example.mainactivity.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainactivity.db.database.AppDB
import com.example.mainactivity.repository.MobiRepository
import com.example.mainactivity.source.StatesData

class SettingsViewModel : ViewModel() {
    lateinit var stateRes: MutableLiveData<List<StatesData>?>

    fun getStates(token: String): LiveData<List<StatesData>?> {
        stateRes = MobiRepository().callStatesAPI(token)
        return stateRes
    }


    fun getLocalStates(context: Context): LiveData<List<StatesData>> {
        val appDataBase = AppDB.getDatabase(context)
        val stateDao = appDataBase?.stateDao()

        return stateDao?.getStatesData()!!
    }
}