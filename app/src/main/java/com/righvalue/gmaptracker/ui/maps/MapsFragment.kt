package com.righvalue.gmaptracker.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.righvalue.gmaptracker.*
import com.righvalue.gmaptracker.R


class MapsFragment : Fragment(), OnRequestPermissionsResult {

    private val TAG = MapsFragment::class.java.simpleName

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var wayLatitude: Double = 0.0
    private var wayLongitude: Double = 0.0

    private val stringBuilder: StringBuilder = StringBuilder()
    private var isGPS = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        val button = root.findViewById<FloatingActionButton>(R.id.btn_start_location_updates)
        button.setOnClickListener { startLocationButtonClick() }

        val activity = requireActivity() as MainActivity
        activity.setRequestPermissionsResult(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)*/

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
                Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~")
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        stringBuilder.append(wayLatitude)
                        stringBuilder.append("-")
                        stringBuilder.append(wayLongitude)
                        stringBuilder.append("\n\n")
                    }
                }
            }
        }

        createLocationHandler()
    }

    private fun createLocationHandler() {
        //Creates a new handler thread
        val locationHandler = HandlerThread("LocationHandler")
        locationHandler.start()

        //Get the looper from the handler thread
        val handler = Handler(locationHandler.looper)
        handler.postDelayed(Runnable{
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

            Log.e(TAG, "~~~~~~~~~~~~~~~")
            //getLastLocation()
            startLocationUpdates()
            createLocationHandler() //Call the create location handler again, this will not be added to the stack because of the looper.
        }, 5000)
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
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

        //Get Last Location
        /*mFusedLocationClient.lastLocation.addOnSuccessListener(this@MainActivity) { location ->
            if (location != null) {
                wayLatitude = location.getLatitude()
                wayLongitude = location.getLongitude()
                txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude))
            } else {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }*/
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //Get Last Location
        mFusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            Log.e(TAG, "333333333333333333333")
            if (location != null) {
                wayLatitude = location.latitude
                wayLongitude = location.longitude
            } else {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }


    private fun updateLocationUI() {
        /*if (mCurrentLocation != null) {
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
        }*/
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out kotlin.String>, grantResults: IntArray) {
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
}