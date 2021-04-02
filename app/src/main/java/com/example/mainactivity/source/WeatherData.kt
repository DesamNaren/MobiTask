package com.example.mainactivity.source

data class WeatherData(
    val coord: coord,
    val weather: List<weather>,
    var base: String,
    var main: main,
    var visibility: Long,
    var wind: wind,
    var clouds: clouds,
    var dt: Long,
    var sys: sys,
    var timezone: Long,
    var id: Long,
    var name: String,
    var cod: Long
)

data class coord(val lat: Double, val lng: Double)

data class weather(val id: Long, val main: String, val description: String, val icon: String)

data class main(
    val temp: Double, val feels_like: Double, val temp_min: Double,
    val temp_max: Double, val pressure: Long, val humidity: Long,
    val sea_level: Long, val grnd_level: Long
)

data class wind(val speed: Double, val deg: Long, val gust: Double)
data class clouds(val all: Long)
data class sys(val country: String, val sunrise: Long, val sunset: Long)
