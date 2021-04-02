package com.example.mainactivity.repository

import android.content.Context
import android.provider.UserDictionary.Words.APP_ID
import androidx.lifecycle.MutableLiveData
import com.cgg.virtuokotlin.network.getNetworkService
import com.cgg.virtuokotlin.network.getWeatherService
import com.example.mainactivity.BuildConfig
import com.example.mainactivity.db.dao.StateDao
import com.example.mainactivity.db.database.AppDB
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.source.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object WatherRepository {
    private val weatherRes = MutableLiveData<WeatherData?>()

    fun callWeatherAPI(name:String): MutableLiveData<WeatherData?> {
//        if (weatherRes.value != null) {
//            return weatherRes
//        }
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

}