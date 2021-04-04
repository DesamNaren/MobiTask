package com.example.mainactivity.ui.map;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.mainactivity.R;
import com.example.mainactivity.adapter.ViewPagerAdapter;
import com.example.mainactivity.adapter.WeatherAdapter;
import com.example.mainactivity.application.MobiApplication;
import com.example.mainactivity.databinding.ActivityWeatherBinding;
import com.example.mainactivity.source.LongWeatherData;
import com.example.mainactivity.source.WeatherInfo;
import com.example.mainactivity.source.WeatherData;
import com.example.mainactivity.source.coord;
import com.example.mainactivity.source.list;
import com.example.mainactivity.source.main;
import com.example.mainactivity.source.main1;
import com.example.mainactivity.source.sys;
import com.example.mainactivity.source.wind;
import com.example.mainactivity.source.wind1;
import com.example.mainactivity.ui.map.RVBaseFragment;
import com.example.mainactivity.ui.map.WeatherViewModel;
import com.example.mainactivity.utilities.AppConstants;
import com.example.mainactivity.utilities.UnitConverter;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class WeatherFragment extends Fragment {

    private ActivityWeatherBinding binding;
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;
    private static final Map<String, Integer> speedUnits = new HashMap<>(3);
    private static final Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;
    Typeface weatherFont;
    WeatherInfo todayWeatherInfo = new WeatherInfo();
    ProgressDialog progressDialog;

    int theme;
    boolean destroyed = false;

    private List<WeatherInfo> longTermWeatherInfo = new ArrayList<>();
    private List<WeatherInfo> longTermTodayWeatherInfo = new ArrayList<>();
    private List<WeatherInfo> longTermTomorrowWeatherInfo = new ArrayList<>();

    public String recentCity = "";
    private ImageView back_button_;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(requireActivity(), R.xml.prefs, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        requireActivity().setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        boolean blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;


        // Initiate activity
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_weather, container, false);
        progressDialog = new ProgressDialog(requireActivity());

        if (darkTheme) {
            binding.toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        } else if (blackTheme) {
            binding.toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Black);
        }

//        setSupportActionBar(binding.toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        Intent intent = requireActivity().getIntent();
        binding.todayTemperature.setText("" + intent.getStringExtra("WEATHER_VALUE"));
        weatherFont = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/weather.ttf");
        binding.todayIcon.setTypeface(weatherFont);
        destroyed = false;

        initMappings();

        updateLastUpdateTime();

        // Set autoupdater
        //AlarmReceiver.setRecurringAlarm(requireActivity());
        return binding.getRoot();
    }


    public WeatherAdapter getAdapter(int id) {
        WeatherAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherAdapter(requireActivity(), longTermTodayWeatherInfo);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherAdapter(requireActivity(), longTermTomorrowWeatherInfo);
        } else {
            weatherRecyclerAdapter = new WeatherAdapter(requireActivity(), longTermWeatherInfo);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTodayWeatherUI();
        updateLongTermWeatherUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getTheme(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("theme", "fresh")) != theme) {
            // Restart activity to apply theme
            requireActivity().overridePendingTransition(0, 0);
            requireActivity().finish();
            requireActivity().overridePendingTransition(0, 0);
            startActivity(requireActivity().getIntent());
        } else if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }


    private void getTodayWeather() {
        SharedPreferences sharedPreferences = MobiApplication.instance.getSharedPreferences(AppConstants.SHARED_PREF, MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");

        WeatherViewModel viewModel = new WeatherViewModel();
        LiveData<WeatherData> liveData = viewModel.getStates(name);
        liveData.observe(requireActivity(), new Observer<WeatherData>() {
            @Override
            public void onChanged(WeatherData weatherData) {
                try {
                    liveData.removeObservers(requireActivity());
                    if (weatherData == null) {
                        notFoundAlert();
                        return;
                    }

                    String city = weatherData.getName();
                    String country = "";
                    sys countryObj = weatherData.getSys();
                    country = countryObj.getCountry();
                    todayWeatherInfo.setSunrise(String.valueOf(countryObj.getSunrise()));
                    todayWeatherInfo.setSunset(String.valueOf(countryObj.getSunset()));
                    todayWeatherInfo.setCity(city);
                    todayWeatherInfo.setCountry(country);

                    coord coordinates = weatherData.getCoord();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity());
                    sp.edit().putFloat("latitude", (float) coordinates.getLat());
                    sp.edit().putFloat("longitude", (float) coordinates.getLng());

                    main main = weatherData.getMain();

                    todayWeatherInfo.setTemperature(String.valueOf(main.getTemp()));
                    todayWeatherInfo.setDescription(weatherData.getWeather().get(0).getDescription());
                    wind windObj = weatherData.getWind();
                    todayWeatherInfo.setWind(String.valueOf(windObj.getSpeed()));
                    if (windObj.getDeg() > 0) {
                        todayWeatherInfo.setWindDirectionDegree((double) windObj.getDeg());
                    } else {
                        todayWeatherInfo.setWindDirectionDegree(null);
                    }
                    todayWeatherInfo.setPressure(String.valueOf(main.getPressure()));
                    todayWeatherInfo.setHumidity(String.valueOf(main.getHumidity()));

//                    JSONObject rainObj = reader.optJSONObject("rain");
//                    String rain;
//                    if (rainObj != null) {
//                        rain = getRainString(rainObj);
//                    } else {
//                        JSONObject snowObj = reader.optJSONObject("snow");
//                        if (snowObj != null) {
//                            rain = getRainString(snowObj);
//                        } else {
//                            rain = "0";
//                        }
//                    }
//                    todayWeather.setRain(rain);

                    final String idString = String.valueOf(weatherData.getWeather().get(0).getId());
                    todayWeatherInfo.setId(idString);
                    todayWeatherInfo.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));


                    updateTodayWeatherUI();
                    updateLastUpdateTime();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        LiveData<LongWeatherData> liveDataLong = viewModel.getLongWeather(name);
        liveDataLong.observe(requireActivity(), new Observer<LongWeatherData>() {
            @Override
            public void onChanged(LongWeatherData longWeatherData) {
                int i;
                try {
                    liveDataLong.removeObservers(requireActivity());

                    if (longWeatherData == null) {
                        notFoundAlert();
                        return;
                    }

                    longTermWeatherInfo = new ArrayList<>();
                    longTermTodayWeatherInfo = new ArrayList<>();
                    longTermTomorrowWeatherInfo = new ArrayList<>();

                    List<list> list = longWeatherData.getList();
                    for (i = 0; i < list.size(); i++) {
                        WeatherInfo weatherInfo = new WeatherInfo();

                        list listItem = list.get(i);
                        main1 main = listItem.getMain();

                        weatherInfo.setDate(String.valueOf(listItem.getDt()));
                        weatherInfo.setTemperature(String.valueOf(main.getTemp()));
                        weatherInfo.setDescription(listItem.getWeather().get(0).getDescription());
                        wind1 windObj = listItem.getWind();
                        weatherInfo.setWind(String.valueOf(windObj.getSpeed()));
                        weatherInfo.setWindDirectionDegree((double) windObj.getDeg());
                        weatherInfo.setPressure(String.valueOf(main.getPressure()));
                        weatherInfo.setHumidity(String.valueOf(main.getHumidity()));

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

                        final String idString = String.valueOf(listItem.getWeather().get(0).getId());
                        weatherInfo.setId(idString);

                        final String dateMsString = listItem.getDt() + "000";
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(Long.parseLong(dateMsString));
                        weatherInfo.setIcon(setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));

                        Calendar today = Calendar.getInstance();
                        if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                            longTermTodayWeatherInfo.add(weatherInfo);
                        } else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
                            longTermTomorrowWeatherInfo.add(weatherInfo);
                        } else {
                            longTermWeatherInfo.add(weatherInfo);
                        }

                        updateLongTermWeatherUI();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = requireActivity().getString(R.string.weather_sunny);
            } else {
                icon = requireActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = requireActivity().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = requireActivity().getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = requireActivity().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = requireActivity().getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = requireActivity().getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = requireActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }


    private void updateTodayWeatherUI() {
        try {
            if (todayWeatherInfo==null || todayWeatherInfo.getCountry().isEmpty()) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        DateFormat timeFormat = null;
        try {
            String city = todayWeatherInfo.getCity();
            String country = todayWeatherInfo.getCountry();
            timeFormat = android.text.format.DateFormat.getTimeFormat(requireActivity().getApplicationContext());
//            requireActivity().getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        // Temperature

        float temperature = 0;
        try {

            temperature = UnitConverter.INSTANCE.INSTANCE.convertTemperature(Float.parseFloat(todayWeatherInfo.getTemperature()), sp);
            if (sp.getBoolean("temperatureInteger", false)) {
                temperature = Math.round(temperature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Rain
        String rainString = null;
        try {
            double rain = Double.parseDouble(todayWeatherInfo.getRain());
            rainString = UnitConverter.INSTANCE.getRainString(rain, sp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeatherInfo.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConverter.INSTANCE.convertWind(wind, sp);

        // Pressure
        double pressure = 0;
        try {
            pressure = UnitConverter.INSTANCE.convertPressure((float) Double.parseDouble(todayWeatherInfo.getPressure()), sp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            binding.todayTemperature.setText(new DecimalFormat("0.#").format(temperature) + " " + sp.getString("unit", "°C"));

            binding.todayDescription.setText(todayWeatherInfo.getDescription().substring(0, 1).toUpperCase() +
                    todayWeatherInfo.getDescription().substring(1));
            if (sp.getString("speedUnit", "m/s").equals("bft")) {
                binding.todayWind.setText(getString(R.string.wind) + ": " +
                        UnitConverter.INSTANCE.getBeaufortName((int) wind) +
                        (todayWeatherInfo.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, requireActivity(), todayWeatherInfo) : ""));
            } else {
                binding.todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                        localize(sp, "speedUnit", "m/s") +
                        (todayWeatherInfo.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, requireActivity(), todayWeatherInfo) : ""));
            }
            binding.todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                    localize(sp, "pressureUnit", "hPa"));
            binding.todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeatherInfo.getHumidity() + " %");
            binding.todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeatherInfo.getSunrise()));
            binding.todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeatherInfo.getSunset()));
            binding.todayIcon.setText(todayWeatherInfo.getIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLongTermWeatherUI() {
        if (destroyed) {
            return;
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RVBaseFragment RVBaseFragmentToday = new RVBaseFragment();
        RVBaseFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(RVBaseFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RVBaseFragment RVBaseFragmentTomorrow = new RVBaseFragment();
        RVBaseFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(RVBaseFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RVBaseFragment RVBaseFragment = new RVBaseFragment();
        RVBaseFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(RVBaseFragment, getString(R.string.later));

        int currentPage = binding.viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabs.setupWithViewPager(binding.viewPager);

        if (currentPage == 0 && longTermTodayWeatherInfo.isEmpty()) {
            currentPage = 0;
        }
        binding.viewPager.setCurrentItem(currentPage, false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean shouldUpdate() {
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getLong("lastUpdate", -1);
        boolean cityChanged = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("cityChanged", false);
        // Update if never checked or last update is longer ago than specified threshold
        return cityChanged || lastUpdate < 0 || (Calendar.getInstance().getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_refresh) {
//            if (isNetworkAvailable()) {
//                getTodayWeather();
//                getLongTermWeather();
//            } else {
//                Snackbar.make(binding.viewApp, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
//            }
//            return true;
//        }
//
//        if (id == R.id.action_search) {
//
//            searchCities();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, requireActivity(), preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, WeatherInfo weatherInfo) {
        try {
            if (Double.parseDouble(weatherInfo.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weatherInfo.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weatherInfo.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }

    private void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(requireActivity()).getLong("lastUpdate", -1)
        );
    }

    @SuppressLint("StringFormatInvalid")
    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            binding.lastUpdate.setText("");
        } else {
            binding.lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(requireActivity(), timeInMillis)));
        }
    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "dark":
                return R.style.AppTheme_NoActionBar_Dark;
            case "black":
                return R.style.AppTheme_NoActionBar_Black;
            case "classic":
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            default:
                return R.style.AppTheme_NoActionBar_Classic_Black;
        }
    }


    void notFoundAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());
        alert.setCancelable(false);
        alert.setMessage("city not found");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requireActivity().finish();
            }
        });
        alert.show();
    }
}