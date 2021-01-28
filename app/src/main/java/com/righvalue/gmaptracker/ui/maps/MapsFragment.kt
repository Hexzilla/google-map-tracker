package com.righvalue.gmaptracker.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.righvalue.gmaptracker.*
import com.righvalue.gmaptracker.R


class MapsFragment : Fragment(), OnMapReadyCallback, OnRequestPermissionsResult {

    private val TAG = MapsFragment::class.java.simpleName

    private lateinit var mMap: GoogleMap
    private lateinit var tracker: Tracker

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var wayLatitude: Double = 0.0
    private var wayLongitude: Double = 0.0

    private var trackingState: Boolean = false
    private var backgroundTracking: Boolean = false
    private val stringBuilder: StringBuilder = StringBuilder()
    private var isGPS = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        val button = root.findViewById<FloatingActionButton>(R.id.btn_start_location_updates)
        button.setOnClickListener { startLocationButtonClick() }

        val activity = requireActivity() as MainActivity
        activity.setRequestPermissionsResult(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        initialize()

        return root
    }

    private fun initialize() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5 * 1000 // 10 seconds
        locationRequest.fastestInterval = 5 * 1000 // 5 seconds

        GpsUtils(requireContext()).turnGPSOn { isGPSEnable -> isGPS = isGPSEnable }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onUpdateLocationResult(locationResult)
            }
        }
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
        Log.e(this.TAG, "onMayReady")
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun onUpdateLocationResult(locationResult: LocationResult?) {
        Log.e(TAG, "onUpdateLocationResult")
        if (locationResult == null) {
            return
        }
        for (location in locationResult.locations) {
            if (location != null) {
                updateLocation(location.latitude, location.longitude)
            }
        }
    }

    private fun startBackgroundTracking() {
        if (backgroundTracking) {
            //Creates a new handler thread
            val locationHandler = HandlerThread("LocationHandler")
            locationHandler.start()

            //Get the looper from the handler thread
            val handler = Handler(locationHandler.looper)
            handler.postDelayed(Runnable {
                //Check if the location service is running, if its not. lets start it!
                /*if (!isMyServiceRunning(LocationService::class.java)) {
                    getApplicationContext().startService(
                        Intent(
                            getApplicationContext(),
                            LocationService::class.java
                        )
                    )
                }

                //Requests a new location from the location service(Feel like it could be done in a less static way)
                LocationService.requestNewLocation()*/

                Log.e(TAG, "background thread")
                getLastLocation()

                startBackgroundTracking()
            }, 2000)
        }
    }

    /*private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context?.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }*/

    private fun checkPermission(permission: kotlin.String) : Boolean {
        val result = ActivityCompat.checkSelfPermission(requireContext(), permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationButtonClick() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(
                arrayOf<kotlin.String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                AppConstants.LOCATION_REQUEST)
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        trackingState = true
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        trackingState = false
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        Log.e(TAG, "getLastLocation")
        mFusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            Log.e(TAG, "getLastLocation-callback: " + (location != null))
            if (location != null) {
                updateLocation(location.latitude, location.longitude)
            } else {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        Log.e(this.TAG, "updateLocation:, $latitude, $longitude")
        //TODO - //tracker.updateLocation(2, latitude, longitude)

        /*val location = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(location).title("Marker in india"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))*/
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out kotlin.String>, grantResults: IntArray) {
        Log.e(TAG, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.LOCATION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        backgroundTracking = false
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause")
        backgroundTracking = true
        startBackgroundTracking()
    }
}