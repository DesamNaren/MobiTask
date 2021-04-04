package com.example.mainactivity.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.mainactivity.R
import com.example.mainactivity.databinding.ActivityMapsBinding
import com.example.mainactivity.interfaces.StatesInterface
import com.example.mainactivity.repository.StateRepository
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.utilities.Extensions.toast
import com.example.mainactivity.utilities.Utils
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, StatesInterface {

    private val listStates = ArrayList<StatesData>()
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(17.3, 78.3)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)
    var repo = StateRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        val binding =
            DataBindingUtil.setContentView<ActivityMapsBinding>(this, R.layout.activity_maps)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            turnOnLocation()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_OK) {
            startActivity(intent)
        }
        if (requestCode == REQUEST_LOCATION_TURN_ON) {
            if (resultCode == RESULT_OK) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                android.app.AlertDialog.Builder(this).setCancelable(false).setTitle(
                    resources.getString(R.string.turnonloc)
                ).setPositiveButton(
                    resources.getString(R.string.ok)
                ) { dialog, which ->
                    dialog.dismiss()
                    turnOnLocation()
                }.setNegativeButton(
                    resources.getString(R.string.cancel)
                ) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }.show()
            }
        }
    }


    private fun turnOnLocation() {
        val googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> if (Utils.checkInternetConnection(this)) {
                    val mapFragment =
                        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                    mapFragment!!.getMapAsync(this)
                } else {
                    val alert = android.app.AlertDialog.Builder(this)
                    alert.setCancelable(false)
                    alert.setMessage("No internet")
                    alert.setPositiveButton(
                        resources.getString(R.string.ok)
                    ) { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    alert.show()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(
                        this, REQUEST_LOCATION_TURN_ON
                    )
                } catch (e: SendIntentException) {
                    Toast.makeText(
                        this,
                        "something wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Toast.makeText(
                    this,
                    "something wrong",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(i)
                }
            }
        }
    }

    private val REQUEST_LOCATION_TURN_ON = 2000

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            if (listStates.isNotEmpty())
                repo.insertStates(this@MapsActivity, listStates, this@MapsActivity)
            else
                toast("No cities added")
        }
        return true
    }


    override fun onMapReady(map: GoogleMap) {
        this.map = map
        //Initialize Google Play Services

        //move map camera

        //move map camera
        map.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation))
        map.animateCamera(CameraUpdateFactory.zoomTo(11f))

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
            }
        } else {
            map.isMyLocationEnabled = true
        }
        map.setOnMapClickListener { point ->
            val addresses = Utils.getLocationAddress(this, point.latitude, point.longitude)
            if (addresses.isNotEmpty()) {
                var final_locality: String? = null
                var subLocality: String? = null
                var locality: String? = null
                if (addresses[0].subLocality != null) {
                    subLocality = addresses[0].subLocality
                }
                if (addresses[0].locality != null) {
                    locality = addresses[0].locality
                }

                if (!TextUtils.isEmpty(subLocality)) {
                    final_locality = subLocality
                } else if (!TextUtils.isEmpty(locality)) {
                    final_locality = locality
                } else if (!TextUtils.isEmpty(addresses[0].getAddressLine(0))) {
                    final_locality = addresses[0].getAddressLine(0)
                }
                if (final_locality != null) {

                    val data = repo.checkFav(final_locality, this@MapsActivity);
                    data.observe(this@MapsActivity, androidx.lifecycle.Observer { stateData ->
                        data.removeObservers(this@MapsActivity)
                        val st = StatesData(0, final_locality, true)
                        listStates.add(st)
                        val marker = MarkerOptions().position(
                            LatLng(
                                point.latitude,
                                point.longitude
                            )
                        )
                            .title(final_locality)
                        map.addMarker(marker)
                        println(point.latitude.toString() + "---" + point.longitude)

                    })
                }
            }
        }

        this.map?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById<FrameLayout>(R.id.map), false
                )
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                return infoWindow
            }
        })
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        showCurrentPlace()
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            -> {
                locationPermissionGranted = true
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                } else {
                    toast("Grant permission")
                    count++;
                    if (count > 1)
                        perCallBack()
                    return

                }
            }
        }
        updateLocationUI()
    }

    var count: Int = 0;

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result
                    val count =
                        if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                            likelyPlaces.placeLikelihoods.size
                        } else {
                            M_MAX_ENTRIES
                        }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLngs = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                        i++
                        if (i > count - 1) {
                            break
                        }
                    }
                    openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            Log.i(TAG, "The user did not grant location permission.")
            map?.addMarker(
                MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet))
            )
            getLocationPermission()
        }
    }

    private fun openPlacesDialog() {
        val listener =
            DialogInterface.OnClickListener { _, which ->
                val markerLatLng = likelyPlaceLatLngs[which]
                var markerSnippet = likelyPlaceAddresses[which]
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = """
                    $markerSnippet
                    ${likelyPlaceAttributions[which]}
                    """.trimIndent()
                }
                map?.addMarker(
                    MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng!!)
                        .snippet(markerSnippet)
                )
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerLatLng,
                        DEFAULT_ZOOM.toFloat()
                    )
                )
            }

        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
    }

    private fun updateLocationUI() {
        try {
            if (map == null) {
                return
            }
            try {
                if (locationPermissionGranted) {
                    map?.isMyLocationEnabled = true
                    map?.uiSettings?.isMyLocationButtonEnabled = true
                } else {
                    try {
                        map?.isMyLocationEnabled = false
                        map?.uiSettings?.isMyLocationButtonEnabled = false

                        lastKnownLocation = null
                        getLocationPermission()
                    } catch (e: Exception) {
                    }

                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 20
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        private const val M_MAX_ENTRIES = 5
    }

    override fun stateCount(count: Int) {
        if (count > 0) {
            toast("Updated")
            val newIntent = Intent(this@MapsActivity, MainActivity::class.java)
            newIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            startActivity(newIntent)
        }
    }

    override fun checkCount(count: Int, name: String) {
        TODO("Not yet implemented")
    }

    override fun stateFav(flag: Boolean, name: String, pos: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(name: String) {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val newIntent = Intent(this@MapsActivity, MainActivity::class.java)
        newIntent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        startActivity(newIntent)
    }


    fun perCallBack() {
        val d = AlertDialog.Builder(this);
        d.setTitle("Grant Permission")
        d.setPositiveButton("OK") { _, _ ->
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
        d.setNegativeButton("Not Now") { _, _ ->
            finish()
        }
            .show()
    }

}