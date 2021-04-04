package com.example.mainactivity.source

import com.example.mainactivity.source.WindDirection.Companion.byDegree
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class WeatherInfo {
    var city: String? = null
    var country: String? = null
    var date: Date? = null
        private set
    var temperature: String? = null
    var description: String? = null
    var wind: String? = null
    var windDirectionDegree: Double? = null

    var pressure: String? = null
    var humidity: String? = null
    var rain: String? = null
    var id: String? = null
    var icon: String? = null
    var lastUpdated: String? = null
    var sunrise: Date? = null
        private set
    var sunset: Date? = null
        private set
    val windDirection: WindDirection
        get() = byDegree(windDirectionDegree!!)

    fun getWindDirection(numberOfDirections: Int): WindDirection {
        return byDegree(windDirectionDegree!!, numberOfDirections)
    }

    val isWindDirectionAvailable: Boolean
        get() = windDirectionDegree != null

    fun setSunrise(dateString: String) {
        try {
            setSunrise(Date(dateString.toLong() * 1000))
        } catch (e: Exception) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            try {
                setSunrise(inputFormat.parse(dateString))
            } catch (e2: ParseException) {
                setSunrise(Date()) // make the error somewhat obvious
                e2.printStackTrace()
            }
        }
    }

    fun setSunrise(date: Date?) {
        sunrise = date
    }

    fun setSunset(dateString: String) {
        try {
            setSunset(Date(dateString.toLong() * 1000))
        } catch (e: Exception) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            try {
                setSunrise(inputFormat.parse(dateString))
            } catch (e2: ParseException) {
                setSunset(Date()) // make the error somewhat obvious
                e2.printStackTrace()
            }
        }
    }

    fun setSunset(date: Date?) {
        sunset = date
    }

    fun setDate(dateString: String) {
        try {
            setDate(Date(dateString.toLong() * 1000))
        } catch (e: Exception) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            try {
                setDate(inputFormat.parse(dateString))
            } catch (e2: ParseException) {
                setDate(Date()) // make the error somewhat obvious
                e2.printStackTrace()
            }
        }
    }

    fun setDate(date: Date?) {
        this.date = date
    }

    fun getNumDaysFrom(initialDate: Date?): Long {
        val initial = Calendar.getInstance()
        initial.time = initialDate
        initial[Calendar.MILLISECOND] = 0
        initial[Calendar.SECOND] = 0
        initial[Calendar.MINUTE] = 0
        initial[Calendar.HOUR_OF_DAY] = 0
        val me = Calendar.getInstance()
        me.time = date
        me[Calendar.MILLISECOND] = 0
        me[Calendar.SECOND] = 0
        me[Calendar.MINUTE] = 0
        me[Calendar.HOUR_OF_DAY] = 0
        return Math.round((me.timeInMillis - initial.timeInMillis) / 86400000.0)
    }

    companion object {
        // you may use values like 4, 8, etc. for numberOfDirections
        fun windDirectionDegreeToIndex(degree: Double, numberOfDirections: Int): Int {
            // to be on the safe side
            var degree = degree
            degree %= 360.0
            if (degree < 0) degree += 360.0
            degree += (180 / numberOfDirections).toDouble() // add offset to make North start from 0
            val direction = Math.floor(degree * numberOfDirections / 360).toInt()
            return direction % numberOfDirections
        }
    }
}