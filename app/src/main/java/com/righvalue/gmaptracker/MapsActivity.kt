package com.righvalue.gmaptracker

import android.annotation.SuppressLint
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private val TAG = MapsActivity::class.java.simpleName

    private lateinit var mMap: GoogleMap
    private lateinit var tracker: Tracker

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mSettingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null

    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
    private val REQUEST_CHECK_SETTINGS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        tracker = Tracker(this)
        init()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        Log.e(this.TAG, "onMayReady")
    }

    private fun init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.e(TAG, "Set Current Location")
                // location is received
                mCurrentLocation = locationResult.lastLocation
                //mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateLocationUI()
            }
        }

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    @OnClick(R.id.btn_start_location_updates)
    public fun startLocationButtonClick() {
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this, OnSuccessListener<Any?> {
                Log.i(this.TAG, "All location settings are satisfied.")
                Toast.makeText(
                    applicationContext,
                    "Started location updates!",
                    Toast.LENGTH_SHORT
                ).show()

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                updateLocationUI()
            })
            .addOnFailureListener(this, OnFailureListener { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(this.TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings")
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this@MapsActivity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sie: SendIntentException) {
                            Log.i(this.TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                        Log.e(this.TAG, errorMessage)
                        Toast.makeText(this@MapsActivity, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                updateLocationUI()
            })
    }

    private fun updateLocationUI() {
        if (mCurrentLocation != null) {
            // location last updated time
            /*Toast.makeText(
                applicationContext, "Lat: " + mCurrentLocation!!.latitude
                        + ", Lng: " + mCurrentLocation!!.longitude, Toast.LENGTH_LONG
            ).show()*/

            val location = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
            Log.e(this.TAG, location.latitude.toString() + "," + location.longitude.toShort())
            tracker.updateLocation(2, location.latitude, location.longitude)

            mMap.addMarker(MarkerOptions().position(location).title("Marker in india"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))
        }
    }
}