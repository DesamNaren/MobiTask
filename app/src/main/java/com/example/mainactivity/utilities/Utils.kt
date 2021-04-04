package com.example.mainactivity.utilities

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import com.example.mainactivity.source.WeatherInfo
import java.util.*
import kotlin.collections.ArrayList

class Utils {
    companion object {

        fun checkInternetConnection(context: Context): Boolean {
            var result = false
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.run {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            } else {
                cm.run {
                    cm.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            result = true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            result = true
                        }
                    }
                }
            }
            return result
        }

        fun getLocationAddress(context: Context?, mLat: Double, mLong: Double): List<Address> {
            var addresses: List<Address> = ArrayList()
            val geoCoder = Geocoder(context, Locale.getDefault())
            try {
                addresses = geoCoder.getFromLocation(mLat, mLong, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    return addresses
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context, e.message, Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
            return addresses
        }


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
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun localize(
            sp: SharedPreferences?,
            preferenceKey: String?,
            defaultValueKey: String?
        ): String? {
            return localize(sp, preferenceKey, defaultValueKey)
        }

    }

}