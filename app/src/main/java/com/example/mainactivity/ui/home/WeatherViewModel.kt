package com.example.mainactivity.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainactivity.db.database.AppDB
import com.example.mainactivity.interfaces.StatesInterface
import com.example.mainactivity.repository.MobiRepository
import com.example.mainactivity.repository.StateRepository
import com.example.mainactivity.repository.WatherRepository
import com.example.mainactivity.source.WeatherData

class WeatherViewModel : ViewModel() {
    lateinit var weatherRes: MutableLiveData<WeatherData?>

    fun getStates(name: String): LiveData<WeatherData?> {
        weatherRes = WatherRepository.callWeatherAPI(name)
        return weatherRes
    }
}