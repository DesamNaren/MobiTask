@file:Suppress("NAME_SHADOWING")

package com.example.mainactivity.utilities

import android.content.Context
import android.content.SharedPreferences
import com.example.mainactivity.R
import com.example.mainactivity.source.WeatherInfo
import java.util.*

object UnitConverter {

    fun getWindDirectionString(
        sp: SharedPreferences,
        context: Context?,
        weatherInfo: WeatherInfo
    ): String? {
        try {
            if (weatherInfo.wind.toDouble() != 0.0) {
                val pref = sp.getString("windDirectionFormat", null)
                if ("arrow" == pref) {
                    return weatherInfo.getWindDirection(8).getArrow(context)
                } else if ("abbr" == pref) {
                    return weatherInfo.windDirection.getLocalizedString(context)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private val speedUnits: MutableMap<String, Int?> = HashMap(3)
    private val pressUnits: MutableMap<String, Int?> = HashMap(3)


    fun localize(
        sp: SharedPreferences,
        context: Context,
        preferenceKey: String,
        defaultValueKey: String?
    ): String? {
        speedUnits["m/s"] = R.string.speed_unit_mps
        speedUnits["kph"] = R.string.speed_unit_kph
        speedUnits["mph"] = R.string.speed_unit_mph
        speedUnits["kn"] = R.string.speed_unit_kn
        pressUnits["hPa"] = R.string.pressure_unit_hpa
        pressUnits["kPa"] = R.string.pressure_unit_kpa
        pressUnits["mm Hg"] = R.string.pressure_unit_mmhg
        val preferenceValue = sp.getString(preferenceKey, defaultValueKey)
        var result = preferenceValue
        if ("speedUnit" == preferenceKey) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits[preferenceValue]!!)
            }
        } else if ("pressureUnit" == preferenceKey) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue)!!)
            }
        }
        return result
    }
    fun convertTemperature(temperature: Float, sp: SharedPreferences): Float {
        return if (sp.getString("unit", "°C") == "°C") {
            kelvinToCelsius(temperature)
        } else if (sp.getString("unit", "°C") == "°F") {
            kelvinToFahrenheit(temperature)
        } else {
            temperature
        }
    }

    private fun kelvinToCelsius(kelvinTemp: Float): Float {
        return kelvinTemp - 273.15f
    }

    private fun kelvinToFahrenheit(kelvinTemp: Float): Float {
        return 9 * kelvinToCelsius(kelvinTemp) / 5 + 32
    }

    fun getRainString(rain: Double, sp: SharedPreferences): String {
        var rain = rain
        return if (rain > 0) {
            if (sp.getString("lengthUnit", "mm") == "mm") {
                if (rain < 0.1) {
                    " (<0.1 mm)"
                } else {
                    String.format(
                        Locale.ENGLISH,
                        " (%.1f %s)",
                        rain,
                        sp.getString("lengthUnit", "mm")
                    )
                }
            } else {
                rain /= 25.4
                if (rain < 0.01) {
                    " (<0.01 in)"
                } else {
                    String.format(
                        Locale.ENGLISH,
                        " (%.2f %s)",
                        rain,
                        sp.getString("lengthUnit", "mm")
                    )
                }
            }
        } else {
            ""
        }
    }

    fun convertPressure(pressure: Float, sp: SharedPreferences): Float {
        return when {
            sp.getString("pressureUnit", "hPa") == "kPa" -> {
                pressure / 10
            }
            sp.getString("pressureUnit", "hPa") == "mm Hg" -> {
                (pressure * 0.750061561303).toFloat()
            }
            sp.getString("pressureUnit", "hPa") == "in Hg" -> {
                (pressure * 0.0295299830714).toFloat()
            }
            else -> {
                pressure
            }
        }
    }

    fun convertWind(wind: Double, sp: SharedPreferences): Double {
        return when {
            sp.getString("speedUnit", "m/s") == "kph" -> {
                wind * 3.6
            }
            sp.getString("speedUnit", "m/s") == "mph" -> {
                wind * 2.23693629205
            }
            sp.getString("speedUnit", "m/s") == "kn" -> {
                wind * 1.943844
            }
            sp.getString("speedUnit", "m/s") == "bft" -> {
                when {
                    wind < 0.3 -> {
                        0.0 // Calm
                    }
                    wind < 1.5 -> {
                        1.0 // Light air
                    }
                    wind < 3.3 -> {
                        2.0 // Light breeze
                    }
                    wind < 5.5 -> {
                        3.0 // Gentle breeze
                    }
                    wind < 7.9 -> {
                        4.0 // Moderate breeze
                    }
                    wind < 10.7 -> {
                        5.0 // Fresh breeze
                    }
                    wind < 13.8 -> {
                        6.0 // Strong breeze
                    }
                    wind < 17.1 -> {
                        7.0 // High wind
                    }
                    wind < 20.7 -> {
                        8.0 // Gale
                    }
                    wind < 24.4 -> {
                        9.0 // Strong gale
                    }
                    wind < 28.4 -> {
                        10.0 // Storm
                    }
                    wind < 32.6 -> {
                        11.0 // Violent storm
                    }
                    else -> {
                        12.0 // Hurricane
                    }
                }
            }
            else -> {
                wind
            }
        }
    }

    fun getBeaufortName(wind: Int): String {
        return when (wind) {
            0 -> {
                "Calm"
            }
            1 -> {
                "Light air"
            }
            2 -> {
                "Light breeze"
            }
            3 -> {
                "Gentle breeze"
            }
            4 -> {
                "Moderate breeze"
            }
            5 -> {
                "Fresh breeze"
            }
            6 -> {
                "Strong breeze"
            }
            7 -> {
                "High wind"
            }
            8 -> {
                "Gale"
            }
            9 -> {
                "Strong gale"
            }
            10 -> {
                "Storm"
            }
            11 -> {
                "Violent storm"
            }
            else -> {
                "Hurricane"
            }
        }
    }
}