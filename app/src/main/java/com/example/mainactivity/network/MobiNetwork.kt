/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cgg.virtuokotlin.network

import com.example.mainactivity.BuildConfig
import com.example.mainactivity.source.LongWeatherData
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.source.TokenData
import com.example.mainactivity.source.WeatherData
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


val VIRTUO_BASE_URL = BuildConfig.SERVER_URL
val WEATHER_BASE_URL = BuildConfig.WEATHER_SERVER_URL

private val service: MobiNetwork by lazy {
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(VIRTUO_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    retrofit.create(MobiNetwork::class.java)
}

private val wService: MobiNetwork by lazy {
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    retrofit.create(MobiNetwork::class.java)
}

fun getNetworkService() = service
fun getWeatherService() = wService

interface MobiNetwork {

    @GET("api/getaccesstoken")
    fun getSessionToken(
        @Header("user-email") user_email: String?,
        @Header("api-token") api_token: String?
    ): Call<TokenData?>?

    @GET("api/states/India")
    fun getStatesAPI(
        @Header("Authorization") authorization: String?
    ): Call<List<StatesData>>?

    @GET("weather")
    fun getWeatherInfoAPI(
        @Query("q") name: String?,
        @Query("appid") id: String?
    ): Call<WeatherData>?

    @GET("forecast")
    fun getLongWeatherInfoAPI(
        @Query("q") name: String?,
        @Query("appid") id: String?
    ): Call<LongWeatherData>?
}

