package com.example.mainactivity.repository

import androidx.lifecycle.MutableLiveData
import com.cgg.virtuokotlin.network.getWeatherService
import com.example.mainactivity.BuildConfig
import com.example.mainactivity.source.LongWeatherData
import com.example.mainactivity.source.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {
    private val weatherRes = MutableLiveData<WeatherData>()
    private val longWeatherRes = MutableLiveData<LongWeatherData>()

    fun callWeatherAPI(name: String): MutableLiveData<WeatherData> {
        if (weatherRes.value != null) {
            return weatherRes
        }
        val vService = getWeatherService()
        val call = vService.getWeatherInfoAPI(name, BuildConfig.APP_ID)

        call!!.enqueue(object : Callback<WeatherData?> {
            override fun onResponse(
                call: Call<WeatherData?>,
                response: Response<WeatherData?>
            ) {
                weatherRes.value = response.body()
            }

            override fun onFailure(call: Call<WeatherData?>, t: Throwable) {
                weatherRes.value = null
            }

        })
        return weatherRes
    }


    fun callLongWeatherAPI(name: String): MutableLiveData<LongWeatherData> {
        if (longWeatherRes.value != null) {
            return longWeatherRes
        }
        val vService = getWeatherService()
        val call = vService.getLongWeatherInfoAPI(name, BuildConfig.APP_ID)

        call!!.enqueue(object : Callback<LongWeatherData?> {
            override fun onResponse(
                call: Call<LongWeatherData?>,
                response: Response<LongWeatherData?>
            ) {
                longWeatherRes.value = response.body()
            }

            override fun onFailure(call: Call<LongWeatherData?>, t: Throwable) {
                longWeatherRes.value = null
            }

        })
        return longWeatherRes
    }

}