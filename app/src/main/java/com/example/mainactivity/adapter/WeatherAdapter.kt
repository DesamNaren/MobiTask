package com.example.mainactivity.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.source.WeatherInfo
import com.example.mainactivity.utilities.UnitConverter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WeatherAdapter(
    private val context: Context,
    private val itemList: List<WeatherInfo>?
) :
    RecyclerView.Adapter<WeatherHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WeatherHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_row, null)
        return WeatherHolder(view)
    }

    @SuppressLint("SetTextI18n", "ResourceType", "SimpleDateFormat")
    override fun onBindViewHolder(customHolder: WeatherHolder, i: Int) {
        try {
            val weatherInfoItem = itemList!![i]
            val sp = PreferenceManager.getDefaultSharedPreferences(context)

            var temperature =
                weatherInfoItem.temperature?.let {
                    UnitConverter.convertTemperature(
                        it.toFloat(),
                        sp
                    )
                }
            if (sp.getBoolean("temperatureInteger", false)) {
                if (temperature != null) {
                    temperature = temperature.roundToInt().toFloat()
                }
            }
            var wind: Double = try {
                weatherInfoItem.wind?.toDouble()!!
            } catch (e: Exception) {
                e.printStackTrace()
                0.0
            }
            wind = UnitConverter.convertWind(wind, sp)

            // Pressure
            val pressure = weatherInfoItem.pressure?.toDouble()?.let {
                UnitConverter.convertPressure(
                    it
                        .toFloat(), sp
                ).toDouble()
            }
            val tz = TimeZone.getDefault()
            val defaultDateFormat = context.resources.getStringArray(R.array.dateFormatsValues)[0]
            var dateFormat = sp.getString("dateFormat", defaultDateFormat)
            if ("custom" == dateFormat) {
                dateFormat = sp.getString("dateFormatCustom", defaultDateFormat)
            }
            var dateString: String
            try {
                val resultFormat = SimpleDateFormat(dateFormat)
                resultFormat.timeZone = tz
                dateString = resultFormat.format(weatherInfoItem.date)
            } catch (e: IllegalArgumentException) {
                dateString = context.resources.getString(R.string.error_dateFormat)
            }
            if (sp.getBoolean("differentiateDaysByTint", false)) {
                val now = Date()
                val color: Int
                if (weatherInfoItem.getNumDaysFrom(now) > 1) {
                    val ta = context.obtainStyledAttributes(
                        intArrayOf(
                            R.attr.colorTintedBackground,
                            R.attr.colorBackground
                        )
                    )
                    color = if (weatherInfoItem.getNumDaysFrom(now) % 2 == 1L) {
                        ta.getColor(
                            0,
                            ContextCompat.getColor(context, R.color.colorTintedBackground)
                        )
                    } else {
                        ta.getColor(1, ContextCompat.getColor(context, R.color.colorBackground))
                    }
                    ta.recycle()
                    customHolder.itemView.setBackgroundColor(color)
                }
            }
            customHolder.itemDate.text = dateString
            if (sp.getBoolean("displayDecimalZeroes", false)) {
                if (temperature != null) {
                    customHolder.itemTemperature.text =
                        DecimalFormat("#.0").format(temperature.toDouble()) + " " + sp.getString(
                            "unit",
                            "°C"
                        )
                }
            } else {
                if (temperature != null) {
                    customHolder.itemTemperature.text =
                        DecimalFormat("#.#").format(temperature.toDouble()) + " " + sp.getString(
                            "unit",
                            "°C"
                        )
                }
            }
            customHolder.itemDescription.text =
                weatherInfoItem.description!!.substring(0, 1).toUpperCase(Locale.getDefault()) +
                        weatherInfoItem.description!!.substring(1)
            val weatherFont = Typeface.createFromAsset(context.assets, "fonts/weather.ttf")
            customHolder.itemIcon.typeface = weatherFont
            customHolder.itemIcon.text = weatherInfoItem.icon
            if (sp.getString("speedUnit", "m/s") == "bft") {
                customHolder.itemWind.text = context.getString(R.string.wind) + ": " +
                        UnitConverter.getBeaufortName(wind.toInt()) + " " + UnitConverter.getWindDirectionString(
                    sp,
                    context, weatherInfoItem
                )
            } else {
                customHolder.itemWind.text =
                    (context.getString(R.string.wind) + ": " + DecimalFormat("#.0").format(wind) + " " +
                            UnitConverter.localize(sp, context, "speedUnit", "m/s")
                            + " " + UnitConverter.getWindDirectionString(
                        sp,
                        context,
                        weatherInfoItem
                    ))
            }
            customHolder.itemPressure.text =
                context.getString(R.string.pressure) + ": " + DecimalFormat("#.0").format(pressure) + " " +
                        UnitConverter.localize(sp, context, "pressureUnit", "hPa")
            customHolder.itemHumidity.text =
                context.getString(R.string.humidity) + ": " + weatherInfoItem.humidity + " %"
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }
}