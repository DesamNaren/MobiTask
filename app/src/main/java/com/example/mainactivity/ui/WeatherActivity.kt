package com.example.mainactivity.ui;


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.mainactivity.R
import com.example.mainactivity.adapter.ViewPagerAdapter
import com.example.mainactivity.adapter.WeatherAdapter
import com.example.mainactivity.application.MobiApplication
import com.example.mainactivity.databinding.ActivityWeatherBinding
import com.example.mainactivity.source.LongWeatherData
import com.example.mainactivity.source.WeatherData
import com.example.mainactivity.source.WeatherInfo
import com.example.mainactivity.ui.map.RVBaseFragment
import com.example.mainactivity.ui.map.WeatherViewModel
import com.example.mainactivity.utilities.AppConstants
import com.example.mainactivity.utilities.UnitConverter
import com.example.mainactivity.utilities.Utils
import org.json.JSONObject
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*


@Suppress("DEPRECATION")
public class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private val NO_UPDATE_REQUIRED_THRESHOLD = 300000
    private val speedUnits: MutableMap<String, Int?> = HashMap(3)
    private val pressUnits: MutableMap<String, Int?> = HashMap(3)
    private var mappingsInitialised = false
    var weatherFont: Typeface? = null
    var todayWeatherInfo = WeatherInfo()
    var progressDialog: ProgressDialog? = null
    var theme_val = 0
    var destroyed = false
    private var longTermWeatherInfo: MutableList<WeatherInfo> = ArrayList()
    private var longTermTodayWeatherInfo: MutableList<WeatherInfo> = ArrayList()
    private var longTermTomorrowWeatherInfo: MutableList<WeatherInfo> = ArrayList()
    var recentCity = ""
    private val back_button_: ImageView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        this.setTheme(getTheme(prefs.getString("theme", "fresh")).also { theme_val = it })
        val darkTheme = theme_val == R.style.AppTheme_NoActionBar_Dark ||
                theme_val == R.style.AppTheme_NoActionBar_Classic_Dark
        val blackTheme = theme_val == R.style.AppTheme_NoActionBar_Black ||
                theme_val == R.style.AppTheme_NoActionBar_Classic_Black
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather)
        progressDialog = ProgressDialog(this)
        if (darkTheme) {
            binding.toolbar.popupTheme = R.style.AppTheme_PopupOverlay_Dark
        } else if (blackTheme) {
            binding.toolbar.popupTheme = R.style.AppTheme_PopupOverlay_Black
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        val intent = this.intent
        binding.todayTemperature.text = "" + intent.getStringExtra("WEATHER_VALUE")
        weatherFont = Typeface.createFromAsset(this.assets, "fonts/weather.ttf")
        binding.todayIcon.setTypeface(weatherFont)
        destroyed = false
        initMappings()
        updateLastUpdateTime()
    }


    fun getAdapter(id: Int): WeatherAdapter {
        return when (id) {
            0 -> {
                WeatherAdapter(this, longTermTodayWeatherInfo)
            }
            1 -> {
                WeatherAdapter(this, longTermTomorrowWeatherInfo)
            }
            else -> {
                WeatherAdapter(this, longTermWeatherInfo)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateTodayWeatherUI()
        updateLongTermWeatherUI()
    }

    override fun onResume() {
        super.onResume()
        when {
            getTheme(
                PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")
            ) != theme_val -> {
                // Restart activity to apply theme
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
            shouldUpdate() && Utils.checkInternetConnection(this) -> {
                getTodayWeather()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }


    @SuppressLint("CommitPrefEdits")
    private fun getTodayWeather() {
        val sharedPreferences =
            MobiApplication.instance.getSharedPreferences(AppConstants.SHARED_PREF, MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "")
        val viewModel = WeatherViewModel()
        val liveData = viewModel.getStates(name!!)
        liveData.observe(this,
            Observer { weatherData ->
                try {
                    liveData.removeObservers(this@WeatherActivity)
                    setBaseWeather(weatherData)
                    updateTodayWeatherUI()
                    updateLastUpdateTime()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        val liveDataLong = viewModel.getLongWeather(name)
        liveDataLong.observe(this,
            Observer { longWeatherData ->
                liveDataLong.removeObservers(this@WeatherActivity)
                setFutureWeather(longWeatherData)
            })
    }

    private fun setFutureWeather(longWeatherData: LongWeatherData?) {
        var i: Int
        try {

            if (longWeatherData == null) {
                notFoundAlert()
                return
            }
            longTermWeatherInfo = ArrayList()
            longTermTodayWeatherInfo = ArrayList()
            longTermTomorrowWeatherInfo = ArrayList()
            val list = longWeatherData.list
            i = 0
            while (i < list.size) {
                val weatherInfo = WeatherInfo()
                val (dt, main, weather, _, windObj) = list[i]
                weatherInfo.setDate(dt.toString())
                weatherInfo.temperature = main.temp.toString()
                weatherInfo.description = weather[0].description
                weatherInfo.wind = windObj.speed.toString()
                weatherInfo.windDirectionDegree = windObj.deg.toDouble()
                weatherInfo.pressure = main.pressure.toString()
                weatherInfo.humidity = main.humidity.toString()

                //                        JSONObject rainObj = listItem.optJSONObject("rain");
                //                        String rain = "";
                //                        if (rainObj != null) {
                //                            rain = getRainString(rainObj);
                //                        } else {
                //                            JSONObject snowObj = listItem.optJSONObject("snow");
                //                            if (snowObj != null) {
                //                                rain = getRainString(snowObj);
                //                            } else {
                //                                rain = "0";
                //                            }
                //                        }
                //                        weatherInfo.setRain(rain);
                val idString = weather[0].id.toString()
                weatherInfo.id = idString
                val dateMsString = dt.toString() + "000"
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateMsString.toLong()
                weatherInfo.icon = setWeatherIcon(
                    idString.toInt(),
                    cal[Calendar.HOUR_OF_DAY]
                )
                val today = Calendar.getInstance()
                if (cal[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
                    longTermTodayWeatherInfo.add(weatherInfo)
                } else if (cal[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] + 1) {
                    longTermTomorrowWeatherInfo.add(weatherInfo)
                } else {
                    longTermWeatherInfo.add(weatherInfo)
                }
                updateLongTermWeatherUI()
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBaseWeather(weatherData: WeatherData?) {
        if (weatherData == null) {
            notFoundAlert()
            return
        }
        val city = weatherData.name
        var country = ""
        val (country1, sunrise, sunset) = weatherData.sys
        country = country1
        todayWeatherInfo.setSunrise(sunrise.toString())
        todayWeatherInfo.setSunset(sunset.toString())
        todayWeatherInfo.city = city
        todayWeatherInfo.country = country
        val (lat, lng) = weatherData.coord
        val sp = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
        sp.edit().putFloat("latitude", lat.toFloat())
        sp.edit().putFloat("longitude", lng.toFloat())
        val main = weatherData.main
        todayWeatherInfo.temperature = main.temp.toString()
        todayWeatherInfo.description = weatherData.weather[0].description
        val windObj = weatherData.wind
        todayWeatherInfo.wind = java.lang.String.valueOf(windObj.speed)
        if (windObj.deg > 0) {
            todayWeatherInfo.windDirectionDegree = windObj.deg.toDouble()
        } else {
            todayWeatherInfo.windDirectionDegree = null
        }
        todayWeatherInfo.pressure = main.pressure.toString()
        todayWeatherInfo.humidity = main.humidity.toString()
        val idString = weatherData.weather[0].id.toString()
        todayWeatherInfo.id = idString
        todayWeatherInfo.icon =
            setWeatherIcon(
                idString.toInt(),
                Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            )
    }

    private fun setWeatherIcon(actualId: Int, hourOfDay: Int): String {
        val id = actualId / 100
        var icon = ""
        if (actualId == 800) {
            icon = if (hourOfDay in 7..19) {
                this.getString(R.string.weather_sunny)
            } else {
                this.getString(R.string.weather_clear_night)
            }
        } else {
            when (id) {
                2 -> icon = this.getString(R.string.weather_thunder)
                3 -> icon = this.getString(R.string.weather_drizzle)
                7 -> icon = this.getString(R.string.weather_foggy)
                8 -> icon = this.getString(R.string.weather_cloudy)
                6 -> icon = this.getString(R.string.weather_snowy)
                5 -> icon = this.getString(R.string.weather_rainy)
            }
        }
        return icon
    }

    @SuppressLint("SetTextI18n")
    private fun updateTodayWeatherUI() {
        try {
            if (todayWeatherInfo.country.isEmpty()) {
                return
            }
        } catch (e: Exception) {
            return
        }
        var timeFormat: DateFormat? = null
        try {
            val city = todayWeatherInfo.city
            val country = todayWeatherInfo.country
            timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)
            supportActionBar!!.setTitle(city + if (country.isEmpty()) "" else ", $country")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
        var temperature = 0f
        try {
            temperature =
                UnitConverter.convertTemperature(todayWeatherInfo.temperature.toFloat(), sp)
            if (sp.getBoolean("temperatureInteger", false)) {
                temperature = Math.round(temperature).toFloat()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var wind: Double = try {
            todayWeatherInfo.wind.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
        wind = UnitConverter.convertWind(wind, sp)

        var pressure = 0.0
        try {
            pressure =
                UnitConverter.convertPressure(todayWeatherInfo.pressure.toDouble().toFloat(), sp)
                    .toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            binding.todayTemperature.text =
                DecimalFormat("0.#").format(temperature.toDouble()) + " " + sp.getString(
                    "unit",
                    "°C"
                )
            binding.todayDescription.text =
                todayWeatherInfo.description.substring(0, 1).toUpperCase(Locale.ROOT) +
                        todayWeatherInfo.description.substring(1)
            if (sp.getString("speedUnit", "m/s") == "bft") {
                binding.todayWind.text = getString(R.string.wind) + ": " +
                        UnitConverter.getBeaufortName(wind.toInt()) +
                        if (todayWeatherInfo.isWindDirectionAvailable) " " + getWindDirectionString(
                            sp,
                            this,
                            todayWeatherInfo
                        ) else ""
            } else {
                binding.todayWind.text =
                    getString(R.string.wind) + ": " + DecimalFormat("#.0").format(wind) + " " +
                            localize(sp, "speedUnit", "m/s") +
                            if (todayWeatherInfo.isWindDirectionAvailable) " " + getWindDirectionString(
                                sp,
                                this,
                                todayWeatherInfo
                            ) else ""
            }
            binding.todayPressure.text =
                getString(R.string.pressure) + ": " + DecimalFormat("#.0").format(pressure) + " " +
                        localize(sp, "pressureUnit", "hPa")
            binding.todayHumidity.text =
                getString(R.string.humidity) + ": " + todayWeatherInfo.humidity + " %"
            binding.todaySunrise.text =
                getString(R.string.sunrise) + ": " + timeFormat!!.format(todayWeatherInfo.sunrise)
            binding.todaySunset.text =
                getString(R.string.sunset) + ": " + timeFormat.format(todayWeatherInfo.sunset)
            binding.todayIcon.text = todayWeatherInfo.icon
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLongTermWeatherUI() {
        if (destroyed) {
            return
        }
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        val bundleToday = Bundle()
        bundleToday.putInt("day", 0)
        val recyclerViewFragmentToday =
            RVBaseFragment()
        recyclerViewFragmentToday.arguments = bundleToday
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today))
        val bundleTomorrow = Bundle()
        bundleTomorrow.putInt("day", 1)
        val recyclerViewFragmentTomorrow =
            RVBaseFragment()
        recyclerViewFragmentTomorrow.arguments = bundleTomorrow
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow))
        val bundle = Bundle()
        bundle.putInt("day", 2)
        val recyclerViewFragment =
            RVBaseFragment()
        recyclerViewFragment.arguments = bundle
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later))
        var currentPage = binding.viewPager.currentItem
        viewPagerAdapter.notifyDataSetChanged()
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        if (currentPage == 0 && longTermTodayWeatherInfo.isEmpty()) {
            currentPage = 0
        }
        binding.viewPager.setCurrentItem(currentPage, false)
    }

    private fun shouldUpdate(): Boolean {
        val lastUpdate =
            PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        val cityChanged =
            PreferenceManager.getDefaultSharedPreferences(this).getBoolean("cityChanged", false)
        return cityChanged || lastUpdate < 0 || Calendar.getInstance().timeInMillis - lastUpdate > NO_UPDATE_REQUIRED_THRESHOLD
    }

    private fun initMappings() {
        if (mappingsInitialised) return
        mappingsInitialised = true
        speedUnits["m/s"] = R.string.speed_unit_mps
        speedUnits["kph"] = R.string.speed_unit_kph
        speedUnits["mph"] = R.string.speed_unit_mph
        speedUnits["kn"] = R.string.speed_unit_kn
        pressUnits["hPa"] = R.string.pressure_unit_hpa
        pressUnits["kPa"] = R.string.pressure_unit_kpa
        pressUnits["mm Hg"] = R.string.pressure_unit_mmhg
    }

    private fun localize(
        sp: SharedPreferences,
        preferenceKey: String,
        defaultValueKey: String
    ): String? {
        return localize(sp, this, preferenceKey, defaultValueKey)
    }

    private fun localize(
        sp: SharedPreferences,
        context: Context,
        preferenceKey: String,
        defaultValueKey: String?
    ): String? {
        val preferenceValue = sp.getString(preferenceKey, defaultValueKey)
        var result = preferenceValue
        if ("speedUnit" == preferenceKey) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue)!!)
            }
        } else if ("pressureUnit" == preferenceKey) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue)!!)
            }
        }
        return result
    }

    private fun getWindDirectionString(
        sp: SharedPreferences,
        context: Context?,
        weatherInfo: WeatherInfo
    ): String {
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

    private fun updateLastUpdateTime() {
        updateLastUpdateTime(
            PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        )
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateLastUpdateTime(timeInMillis: Long) {
        if (timeInMillis < 0) {
            binding.lastUpdate.text = ""
        } else {
            binding.lastUpdate.text =
                getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis))
        }
    }

    private fun formatTimeWithDayIfNotToday(context: Context?, timeInMillis: Long): String? {
        val now = Calendar.getInstance()
        val lastCheckedCal: Calendar = GregorianCalendar()
        lastCheckedCal.timeInMillis = timeInMillis
        val lastCheckedDate = Date(timeInMillis)
        val timeFormat =
            android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate)
        return if (now[Calendar.YEAR] == lastCheckedCal[Calendar.YEAR] &&
            now[Calendar.DAY_OF_YEAR] == lastCheckedCal[Calendar.DAY_OF_YEAR]
        ) {
            timeFormat
        } else {
            android.text.format.DateFormat.getDateFormat(context)
                .format(lastCheckedDate) + " " + timeFormat
        }
    }

    private fun getTheme(themePref: String?): Int {
        return when (themePref) {
            "dark" -> R.style.AppTheme_NoActionBar_Dark
            "black" -> R.style.AppTheme_NoActionBar_Black
            "classic" -> R.style.AppTheme_NoActionBar_Classic
            "classicdark" -> R.style.AppTheme_NoActionBar_Classic_Dark
            else -> R.style.AppTheme_NoActionBar_Classic_Black
        }
    }


    private fun notFoundAlert() {
        val alert = AlertDialog.Builder(this@WeatherActivity)
        alert.setCancelable(false)
        alert.setMessage("city not found")
        alert.setPositiveButton(
            "OK"
        ) { _, _ -> finish() }
        alert.show()
    }

}
