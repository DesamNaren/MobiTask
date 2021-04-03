package com.example.mainactivity.source

import com.google.gson.annotations.SerializedName

data class LongWeatherData(
    var cod: Long,
    var message: Long,
    var cnt: Long,
    var list: List<list>,
    var city: city
)

data class list(
    val dt: Long,
    @SerializedName("main")
    val main: main1,
    @SerializedName("weather")
    val weather: List<weather1>,
    @SerializedName("clouds")
    val clouds: clouds1,
    @SerializedName("wind")
    val wind: wind1,
    val visibility: Long,
    val pop: Double,
    @SerializedName("sys")
    val sys: sys1,
    val dt_text: String
)

data class main1(
    val temp: Double, val feels_like: Double, val temp_min: Double,
    val temp_max: Double, val pressure: Long, val humidity: Long,
    val sea_level: Long, val grnd_level: Long, val temp_kf: Double
)

data class weather1(val id: Long, val main: String, val description: String, val icon: String)

data class clouds1(val all: Long)

data class wind1(val speed: Double, val deg: Long)

data class sys1(val pop: String)


data class city(
    val id: Long, val name: String,
    @SerializedName("coord")
    val coord: coord1,
    val country: String,
    val population: Long, val timezone: Long, val sunrise: Long, val sunset: Long
)

data class coord1(val lat: Double, val lng: Double)
