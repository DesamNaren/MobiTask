package com.example.mainactivity.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainactivity.repository.WeatherRepository
import com.example.mainactivity.source.LongWeatherData
import com.example.mainactivity.source.WeatherData

class WeatherViewModel : ViewModel() {
    var weatherRes = MutableLiveData<WeatherData>()
    var longWeatherRes = MutableLiveData<LongWeatherData>()

    fun getStates(name: String): LiveData<WeatherData> {
        weatherRes = WeatherRepository().callWeatherAPI(name)
        return weatherRes
    }


    fun getLongWeather(name: String): LiveData<LongWeatherData> {
        longWeatherRes = WeatherRepository().callLongWeatherAPI(name)
        return longWeatherRes
    }
}