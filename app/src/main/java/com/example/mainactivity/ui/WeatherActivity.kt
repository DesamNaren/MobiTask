//package com.example.mainactivity.ui
//
//import android.annotation.SuppressLint
//import android.app.ProgressDialog
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Typeface
//import android.net.ConnectivityManager
//import android.os.Bundle
//import android.preference.PreferenceManager
//import android.text.InputType
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.Observer
//import com.example.mainactivity.GenericRequestTask
//import com.example.mainactivity.R
//import com.example.mainactivity.adapter.ViewPagerAdapter
//import com.example.mainactivity.adapter.WeatherRecyclerAdapter
//import com.example.mainactivity.databinding.ActivityScrollingBinding
//import com.example.mainactivity.source.*
//import com.example.mainactivity.ui.home.WeatherViewModel
//import com.example.mainactivity.utilities.AppConstants
//import com.example.mainactivity.utilities.UnitConvertor
//import org.json.JSONException
//import org.json.JSONObject
//import java.text.DateFormat
//import java.text.DecimalFormat
//import java.util.*
//
//class WeatherActivity : AppCompatActivity() {
//
//    lateinit var binding: ActivityScrollingBinding
//    // Time in milliseconds; only reload weather if last update is longer ago than this value
//    val NO_UPDATE_REQUIRED_THRESHOLD = 300000
//
//    val speedUnits: MutableMap<String, Int?> = HashMap(3)
//    val pressUnits: MutableMap<String, Int?> = HashMap(3)
//    var mappingsInitialised = false
//
//    var weatherFont: Typeface? = null
//    val todayWeatherInfo = WeatherInfo()
//
//
//    var progressDialog: ProgressDialog? = null
//
//    var theme = 0
//    var destroyed = false
//
//    var longTermWeatherInfo: MutableList<WeatherInfo> = ArrayList()
//    var longTermTodayWeatherInfo: MutableList<WeatherInfo> = ArrayList()
//    var longTermTomorrowWeatherInfo: MutableList<WeatherInfo> = ArrayList()
//
//    var recentCity: String = ""
//    val back_button_: ImageView? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        // Initialize the associated SharedPreferences file with default values
//        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
//        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
//        prefs.getString("theme", "fresh")?.let { getTheme(it).also({ theme = it }) }?.let {
//            setTheme(
//                it
//            )
//        }
//        val darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
//                theme == R.style.AppTheme_NoActionBar_Classic_Dark
//        val blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
//                theme == R.style.AppTheme_NoActionBar_Classic_Black
//
//        // Initiate activity
//        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_scrolling)
//        progressDialog = ProgressDialog(this@WeatherActivity)
//        setSupportActionBar(binding.toolbar)
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true)
//        binding.toolbar.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
//        if (darkTheme) {
//            binding.toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark)
//        } else if (blackTheme) {
//            binding.toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Black)
//        }
//        val intent: Intent = getIntent()
//        binding.todayTemperature.setText("" + intent.getStringExtra("WEATHER_VALUE"))
//        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf")
//        binding.todayIcon.setTypeface(weatherFont)
//        destroyed = false
//        initMappings()
//        updateLastUpdateTime()
//
//        // Set autoupdater
//        //AlarmReceiver.setRecurringAlarm(this);
//    }
//
//
//    fun getAdapter(id: Int): WeatherRecyclerAdapter? {
//        val weatherRecyclerAdapter: WeatherRecyclerAdapter
//        if (id == 0) {
//            weatherRecyclerAdapter = WeatherRecyclerAdapter(this, longTermTodayWeatherInfo)
//        } else if (id == 1) {
//            weatherRecyclerAdapter = WeatherRecyclerAdapter(this, longTermTomorrowWeatherInfo)
//        } else {
//            weatherRecyclerAdapter = WeatherRecyclerAdapter(this, longTermWeatherInfo)
//        }
//        return weatherRecyclerAdapter
//    }
//
//    fun onStart() {
//        super.onStart()
//        updateTodayWeatherUI()
//        updateLongTermWeatherUI()
//    }
//
//    fun onResume() {
//        super.onResume()
//        if (getTheme(
//                PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")
//            ) != theme
//        ) {
//            // Restart activity to apply theme
//            overridePendingTransition(0, 0)
//            finish()
//            overridePendingTransition(0, 0)
//            startActivity(getIntent())
//        } else if (shouldUpdate() && isNetworkAvailable()) {
//            getTodayWeather()
//            getLongTermWeather()
//        }
//    }
//
//    fun onDestroy() {
//        super.onDestroy()
//        destroyed = true
//    }
//
//
//    open fun getTodayWeather() {
//        val viewModel = WeatherViewModel()
//        viewModel.weatherRes.observe(this,
//            Observer<WeatherData> { weatherData ->
//                try {
//                    if (404L == weatherData.cod) {
//                        val result = ParseResult.CITY_NOT_FOUND
//                    }
//                    val city = weatherData.name
//                    var country: String = ""
//                    val countryObj = weatherData.sys
//                    country = countryObj.country
//                    todayWeatherInfo.setSunrise(countryObj.sunrise.toString())
//                    todayWeatherInfo.setSunset(countryObj.sunset.toString())
//                    todayWeatherInfo.city = city
//                    todayWeatherInfo.country = country
//                    val coordinates = weatherData.coord
//                    val sp = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
//                    sp.edit().putFloat("latitude", coordinates.lat.toFloat())
//                    sp.edit().putFloat("longitude", coordinates.lng.toFloat())
//                    val main = weatherData.main
//                    todayWeatherInfo.temperature = main.temp.toString()
//                    todayWeatherInfo.description = weatherData.weather.get(0).description
//                    val windObj = weatherData.wind
//                    todayWeatherInfo.wind = windObj.speed.toString()
//                    if (windObj.deg > 0) {
//                        todayWeatherInfo.windDirectionDegree =
//                            java.lang.Double.valueOf(windObj.deg.toDouble())
//                    } else {
//                        todayWeatherInfo.windDirectionDegree = null
//                    }
//                    todayWeatherInfo.pressure = main.pressure.toString()
//                    todayWeatherInfo.humidity = main.humidity.toString()
//
//                    //                    JSONObject rainObj = reader.optJSONObject("rain");
//                    //                    String rain;
//                    //                    if (rainObj != null) {
//                    //                        rain = getRainString(rainObj);
//                    //                    } else {
//                    //                        JSONObject snowObj = reader.optJSONObject("snow");
//                    //                        if (snowObj != null) {
//                    //                            rain = getRainString(snowObj);
//                    //                        } else {
//                    //                            rain = "0";
//                    //                        }
//                    //                    }
//                    //                    todayWeather.setRain(rain);
//                    val idString = weatherData.weather[0].id.toString()
//                    todayWeatherInfo.id = idString
//                    todayWeatherInfo.icon =
//                        setWeatherIcon(
//                            idString.toInt(),
//                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//                        )
//
//                    updateTodayWeatherUI()
//                    updateLastUpdateTime()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            })
//    }
//
//    open fun getLongTermWeather() {
//        LongTermWeatherTask(this, this, progressDialog).execute()
//    }
//
//    @SuppressLint("RestrictedApi")
//    open fun searchCities() {
//        val alert = AlertDialog.Builder(this)
//        alert.setTitle(this.getString(R.string.search_title))
//        val input = EditText(this)
//        input.inputType = InputType.TYPE_CLASS_TEXT
//        input.maxLines = 1
//        input.isSingleLine = true
//        alert.setView(input, 32, 0, 32, 0)
//        alert.setPositiveButton(R.string.dialog_ok,
//            DialogInterface.OnClickListener { dialog, whichButton ->
//                val result = input.text.toString()
//                if (input.text.toString().length == 0) {
//                    Toast.makeText(
//                        this@WeatherActivity,
//                        "Please enter your city",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    if (!result.isEmpty()) {
//                        saveLocation(result)
//                    }
//                }
//            })
//        alert.setNegativeButton(R.string.dialog_cancel,
//            DialogInterface.OnClickListener { dialog, whichButton ->
//                // Cancelled
//            })
//        alert.show()
//    }
//
//    private fun saveLocation(result: String) {
//        val preferences = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
//        recentCity = preferences.getString("city", AppConstants.DEFAULT_CITY)!!
//        val editor = preferences.edit()
//        editor.putString("city", result)
//        editor.commit()
//        if (recentCity != result) {
//            // New location, update weather
//            getTodayWeather()
//            getLongTermWeather()
//        }
//    }
//
//    open fun setWeatherIcon(actualId: Int, hourOfDay: Int): String? {
//        val id = actualId / 100
//        var icon: String = ""
//        if (actualId == 800) {
//            if (hourOfDay >= 7 && hourOfDay < 20) {
//                icon = this.getString(R.string.weather_sunny)
//            } else {
//                icon = this.getString(R.string.weather_clear_night)
//            }
//        } else {
//            when (id) {
//                2 -> icon = this.getString(R.string.weather_thunder)
//                3 -> icon = this.getString(R.string.weather_drizzle)
//                7 -> icon = this.getString(R.string.weather_foggy)
//                8 -> icon = this.getString(R.string.weather_cloudy)
//                6 -> icon = this.getString(R.string.weather_snowy)
//                5 -> icon = this.getString(R.string.weather_rainy)
//            }
//        }
//        return icon
//    }
//
//    fun getRainString(rainObj: JSONObject?): String {
//        var rain = "0"
//        if (rainObj != null) {
//            rain = rainObj.optString("3h", "fail")
//            if ("fail" == rain) {
//                rain = rainObj.optString("1h", "0")
//            }
//        }
//        return rain
//    }
//
//    open fun parseTodayJson(result: String): ParseResult {
//        try {
//            val reader = JSONObject(result)
//            val code = reader.optString("cod")
//            if ("404" == code) {
//                return ParseResult.CITY_NOT_FOUND
//            }
//            val city = reader.getString("name")
//            var country: String = ""
//            val countryObj = reader.optJSONObject("sys")
//            if (countryObj != null) {
//                country = countryObj.getString("country")
//                todayWeatherInfo.setSunrise(countryObj.getString("sunrise"))
//                todayWeatherInfo.setSunset(countryObj.getString("sunset"))
//            }
//            todayWeatherInfo.city = city
//            todayWeatherInfo.country = country
//            val coordinates = reader.getJSONObject("coord")
//            if (coordinates != null) {
//                val sp = PreferenceManager.getDefaultSharedPreferences(this)
//                sp.edit().putFloat("latitude", coordinates.getDouble("lon").toFloat()).putFloat(
//                    "longitude",
//                    coordinates.getDouble("lat").toFloat()
//                ).commit()
//            }
//            val main = reader.getJSONObject("main")
//            todayWeatherInfo.temperature = main.getString("temp")
//            todayWeatherInfo.description =
//                reader.getJSONArray("weather").getJSONObject(0).getString("description")
//            val windObj = reader.getJSONObject("wind")
//            todayWeatherInfo.wind = windObj.getString("speed")
//            if (windObj.has("deg")) {
//                todayWeatherInfo.windDirectionDegree = windObj.getDouble("deg")
//            } else {
//                todayWeatherInfo.windDirectionDegree = null
//            }
//            todayWeatherInfo.pressure = main.getString("pressure")
//            todayWeatherInfo.humidity = main.getString("humidity")
//            val rainObj = reader.optJSONObject("rain")
//            val rain: String
//            if (rainObj != null) {
//                rain = getRainString(rainObj)
//            } else {
//                val snowObj = reader.optJSONObject("snow")
//                if (snowObj != null) {
//                    rain = getRainString(snowObj)
//                } else {
//                    rain = "0"
//                }
//            }
//            todayWeatherInfo.rain = rain
//            val idString = reader.getJSONArray("weather").getJSONObject(0).getString("id")
//            todayWeatherInfo.id = idString
//            todayWeatherInfo.icon =
//                setWeatherIcon(idString.toInt(), Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
//            val editor =
//                PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
//            editor.putString("lastToday", result)
//            editor.commit()
//        } catch (e: JSONException) {
//            e.printStackTrace()
//            return ParseResult.JSON_EXCEPTION
//        }
//        return ParseResult.OK
//    }
//
//    open fun updateTodayWeatherUI() {
//        try {
//            if (todayWeatherInfo.country.isEmpty()) {
//                return
//            }
//        } catch (e: Exception) {
//            return
//        }
//        var timeFormat: DateFormat? = null
//        try {
//            val city = todayWeatherInfo.city
//            val country = todayWeatherInfo.country
//            timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext())
//            getSupportActionBar()!!.setTitle(city + if (country.isEmpty()) "" else ", $country")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        val sp = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
//
//        // Temperature
//        var temperature = 0f
//        try {
//            temperature =
//                UnitConvertor.convertTemperature(todayWeatherInfo.temperature.toFloat(), sp)
//            if (sp.getBoolean("temperatureInteger", false)) {
//                temperature = Math.round(temperature).toFloat()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        // Rain
//        var rainString: String? = null
//        try {
//            val rain = todayWeatherInfo.rain.toDouble()
//            rainString = UnitConvertor.getRainString(rain, sp)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        // Wind
//        var wind: Double
//        try {
//            wind = todayWeatherInfo.wind.toDouble()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            wind = 0.0
//        }
//        wind = UnitConvertor.convertWind(wind, sp)
//
//        // Pressure
//        var pressure = 0.0
//        try {
//            pressure =
//                UnitConvertor.convertPressure(todayWeatherInfo.pressure.toDouble().toFloat(), sp)
//                    .toDouble()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        try {
//            // binding.todayTemperature.setText(new DecimalFormat("0.#").format(temperature) + " " + sp.getString("unit", "°C"));
//            binding.todayDescription.setText(
//                todayWeatherInfo.description.substring(0, 1).toUpperCase() +
//                        todayWeatherInfo.description.substring(1) + rainString
//            )
//            if ((sp.getString("speedUnit", "m/s") == "bft")) {
//                binding.todayWind.setText(
//                    (getString(R.string.wind).toString() + ": " +
//                            UnitConvertor.getBeaufortName(wind.toInt()) +
//                            (if (todayWeatherInfo.isWindDirectionAvailable) " " + getWindDirectionString(
//                                sp,
//                                this,
//                                todayWeatherInfo
//                            ) else ""))
//                )
//            } else {
//                binding.todayWind.setText(
//                    (getString(R.string.wind).toString() + ": " + DecimalFormat("#.0").format(wind) + " " +
//                            localize(sp, "speedUnit", "m/s") +
//                            (if (todayWeatherInfo.isWindDirectionAvailable) " " + getWindDirectionString(
//                                sp,
//                                this,
//                                todayWeatherInfo
//                            ) else ""))
//                )
//            }
//            binding.todayPressure.setText(
//                (getString(R.string.pressure).toString() + ": " + DecimalFormat("#.0").format(
//                    pressure
//                ) + " " +
//                        localize(sp, "pressureUnit", "hPa"))
//            )
//            binding.todayHumidity.setText(getString(R.string.humidity).toString() + ": " + todayWeatherInfo.humidity + " %")
//            binding.todaySunrise.setText(
//                getString(R.string.sunrise).toString() + ": " + timeFormat!!.format(
//                    todayWeatherInfo.sunrise
//                )
//            )
//            binding.todaySunset.setText(
//                getString(R.string.sunset).toString() + ": " + timeFormat.format(
//                    todayWeatherInfo.sunset
//                )
//            )
//            binding.todayIcon.setText(todayWeatherInfo.icon)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun parseLongTermJson(result: String?): ParseResult {
//        var i: Int
//        try {
//            val reader = JSONObject(result)
//            val code = reader.optString("cod")
//            if (("404" == code)) {
//                if (longTermWeatherInfo == null) {
//                    longTermWeatherInfo = ArrayList()
//                    longTermTodayWeatherInfo = ArrayList()
//                    longTermTomorrowWeatherInfo = ArrayList()
//                }
//                return ParseResult.CITY_NOT_FOUND
//            }
//            longTermWeatherInfo = ArrayList()
//            longTermTodayWeatherInfo = ArrayList()
//            longTermTomorrowWeatherInfo = ArrayList()
//            val list = reader.getJSONArray("list")
//            i = 0
//            while (i < list.length()) {
//                val weatherInfo = WeatherInfo()
//                val listItem = list.getJSONObject(i)
//                val main = listItem.getJSONObject("main")
//                weatherInfo.setDate(listItem.getString("dt"))
//                weatherInfo.temperature = main.getString("temp")
//                weatherInfo.description =
//                    listItem.optJSONArray("weather").getJSONObject(0).getString("description")
//                val windObj = listItem.optJSONObject("wind")
//                if (windObj != null) {
//                    weatherInfo.wind = windObj.getString("speed")
//                    weatherInfo.windDirectionDegree = windObj.getDouble("deg")
//                }
//                weatherInfo.pressure = main.getString("pressure")
//                weatherInfo.humidity = main.getString("humidity")
//                val rainObj = listItem.optJSONObject("rain")
//                var rain: String = ""
//                if (rainObj != null) {
//                    rain = getRainString(rainObj)
//                } else {
//                    val snowObj = listItem.optJSONObject("snow")
//                    if (snowObj != null) {
//                        rain = getRainString(snowObj)
//                    } else {
//                        rain = "0"
//                    }
//                }
//                weatherInfo.rain = rain
//                val idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id")
//                weatherInfo.id = idString
//                val dateMsString = listItem.getString("dt") + "000"
//                val cal = Calendar.getInstance()
//                cal.timeInMillis = dateMsString.toLong()
//                weatherInfo.icon = setWeatherIcon(idString.toInt(), cal.get(Calendar.HOUR_OF_DAY))
//                val today = Calendar.getInstance()
//                if (cal[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
//                    longTermTodayWeatherInfo.add(weatherInfo)
//                } else if (cal[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] + 1) {
//                    longTermTomorrowWeatherInfo.add(weatherInfo)
//                } else {
//                    longTermWeatherInfo.add(weatherInfo)
//                }
//                i++
//            }
//            val editor =
//                PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
//            editor.putString("lastLongterm", result)
//            editor.commit()
//        } catch (e: JSONException) {
//            e.printStackTrace()
//            return ParseResult.JSON_EXCEPTION
//        }
//        return ParseResult.OK
//    }
//
//    open fun updateLongTermWeatherUI() {
//        if (destroyed) {
//            return
//        }
//        val viewPagerAdapter = ViewPagerAdapter(getSupportFragmentManager())
//        val bundleToday = Bundle()
//        bundleToday.putInt("day", 0)
//        val recyclerViewFragmentToday = RecyclerViewFragment()
//        recyclerViewFragmentToday.arguments = bundleToday
//        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today))
//        val bundleTomorrow = Bundle()
//        bundleTomorrow.putInt("day", 1)
//        val recyclerViewFragmentTomorrow = RecyclerViewFragment()
//        recyclerViewFragmentTomorrow.arguments = bundleTomorrow
//        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow))
//        val bundle = Bundle()
//        bundle.putInt("day", 2)
//        val recyclerViewFragment = RecyclerViewFragment()
//        recyclerViewFragment.arguments = bundle
//        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later))
//        var currentPage: Int = binding.viewPager.getCurrentItem()
//        viewPagerAdapter.notifyDataSetChanged()
//        binding.viewPager.setAdapter(viewPagerAdapter)
//        binding.tabs.setupWithViewPager(binding.viewPager)
//        if (currentPage == 0 && longTermTodayWeatherInfo.isEmpty()) {
//            currentPage = 1
//        }
//        binding.viewPager.setCurrentItem(currentPage, false)
//    }
//
//    open fun isNetworkAvailable(): Boolean {
//        val connectivityManager =
//            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetworkInfo = connectivityManager.activeNetworkInfo
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected
//    }
//
//    open fun shouldUpdate(): Boolean {
//        val lastUpdate =
//            PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
//        val cityChanged =
//            PreferenceManager.getDefaultSharedPreferences(this).getBoolean("cityChanged", false)
//        // Update if never checked or last update is longer ago than specified threshold
//        return cityChanged || (lastUpdate < 0) || ((Calendar.getInstance().timeInMillis - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD)
//    }
//
//  override  fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        getMenuInflater().inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//   override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//
////        if (id == R.id.action_refresh) {
////            if (isNetworkAvailable()) {
////                getTodayWeather();
////                getLongTermWeather();
////            } else {
////                Snackbar.make(binding.viewApp, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
////            }
////            return true;
////        }
////
////        if (id == R.id.action_search) {
////
////            searchCities();
////            return true;
////        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    fun initMappings() {
//        if (mappingsInitialised) return
//        mappingsInitialised = true
//        speedUnits["m/s"] = R.string.speed_unit_mps
//        speedUnits["kph"] = R.string.speed_unit_kph
//        speedUnits["mph"] = R.string.speed_unit_mph
//        speedUnits["kn"] = R.string.speed_unit_kn
//        pressUnits["hPa"] = R.string.pressure_unit_hpa
//        pressUnits["kPa"] = R.string.pressure_unit_kpa
//        pressUnits["mm Hg"] = R.string.pressure_unit_mmhg
//    }
//
//    open fun localize(
//        sp: SharedPreferences,
//        preferenceKey: String,
//        defaultValueKey: String
//    ): String? {
//        return localize(sp, this, preferenceKey, defaultValueKey)
//    }
//
//    fun localize(
//        sp: SharedPreferences,
//        context: Context,
//        preferenceKey: String,
//        defaultValueKey: String?
//    ): String? {
//        val preferenceValue = sp.getString(preferenceKey, defaultValueKey)
//        var result = preferenceValue
//        if (("speedUnit" == preferenceKey)) {
//            if (speedUnits.containsKey(preferenceValue)) {
//                result = context.getString(speedUnits.get(preferenceValue)!!)
//            }
//        } else if (("pressureUnit" == preferenceKey)) {
//            if (pressUnits.containsKey(preferenceValue)) {
//                result = context.getString(pressUnits.get(preferenceValue)!!)
//            }
//        }
//        return result
//    }
//
//    fun getWindDirectionString(
//        sp: SharedPreferences,
//        context: Context?,
//        weatherInfo: WeatherInfo
//    ): String? {
//        try {
//            if (weatherInfo.wind.toDouble() != 0.0) {
//                val pref = sp.getString("windDirectionFormat", null)
//                if (("arrow" == pref)) {
//                    return weatherInfo.getWindDirection(8).getArrow(context)
//                } else if (("abbr" == pref)) {
//                    return weatherInfo.windDirection.getLocalizedString(context)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return ""
//    }
//
//    class LongTermWeatherTask(
//        context: Context?,
//        activity: WeatherActivity?,
//        progressDialog: ProgressDialog?
//    ) :
//        GenericRequestTask(context, activity, progressDialog) {
//        override fun parseResponse(response: String): ParseResult {
//            return parseLongTermJson(response)
//        }
//
//        override fun getAPIName(): String {
//            return "forecast"
//        }
//
//        override fun updateMainUI() {
//            updateLongTermWeatherUI()
//        }
//    }
//
//    class ProvideCityNameTask(
//        context: Context?,
//        activity: WeatherActivity?,
//        progressDialog: ProgressDialog?
//    ) :
//        GenericRequestTask(context, activity, progressDialog) {
//        override fun onPreExecute() { /*Nothing*/
//        }
//
//        override fun getAPIName(): String {
//            return "weather"
//        }
//
//        override fun parseResponse(response: String): ParseResult {
//            try {
//                val reader = JSONObject(response)
//                val code = reader.optString("cod")
//                if (("404" == code)) {
//                    return ParseResult.CITY_NOT_FOUND
//                }
//                val city = reader.getString("name")
//                var country = ""
//                val countryObj = reader.optJSONObject("sys")
//                if (countryObj != null) {
//                    country = ", " + countryObj.getString("country")
//                }
////                saveLocation(city + country)
//            } catch (e: JSONException) {
//                e.printStackTrace()
//                return ParseResult.JSON_EXCEPTION
//            }
//            return ParseResult.OK
//        }
//
//        override fun onPostExecute(output: TaskOutput) {
//            /* Handle possible errors only */
//            handleTaskOutput(output)
//        }
//    }
//
//    fun saveLastUpdateTime(sp: SharedPreferences): Long {
//        val now = Calendar.getInstance()
//        sp.edit().putLong("lastUpdate", now.timeInMillis).apply()
//        return now.timeInMillis
//    }
//
//    open fun updateLastUpdateTime() {
//        updateLastUpdateTime(
//            PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
//        )
//    }
//
//    @SuppressLint("StringFormatInvalid")
//    open fun updateLastUpdateTime(timeInMillis: Long) {
//        if (timeInMillis < 0) {
//            // No time
//            binding.lastUpdate.setText("")
//        } else {
//            binding.lastUpdate.setText(
//                getString(
//                    R.string.last_update,
//                    formatTimeWithDayIfNotToday(this, timeInMillis)
//                )
//            )
//        }
//    }
//
//    fun formatTimeWithDayIfNotToday(context: Context?, timeInMillis: Long): String? {
//        val now = Calendar.getInstance()
//        val lastCheckedCal: Calendar = GregorianCalendar()
//        lastCheckedCal.timeInMillis = timeInMillis
//        val lastCheckedDate = Date(timeInMillis)
//        val timeFormat =
//            android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate)
//        return if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
//            now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)
//        ) {
//            // Same day, only show time
//            timeFormat
//        } else {
//            android.text.format.DateFormat.getDateFormat(context)
//                .format(lastCheckedDate) + " " + timeFormat
//        }
//    }
//
//    open fun getTheme(themePref: String): Int {
//        when (themePref) {
//            "dark" -> return R.style.AppTheme_NoActionBar_Dark
//            "black" -> return R.style.AppTheme_NoActionBar_Black
//            "classic" -> return R.style.AppTheme_NoActionBar_Classic
//            "classicdark" -> return R.style.AppTheme_NoActionBar_Classic_Dark
//            "classicblack" -> return R.style.AppTheme_NoActionBar_Classic_Black
//            else -> return R.style.Theme_AppCompat_NoActionBar
//        }
//    }
//
//    override fun onBackPressed() {
//        // super.onBackPressed();
//        val intent = Intent(this@WeatherActivity, MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(intent)
//        finish()
//    }
//}