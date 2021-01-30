package com.righvalue.gmaptracker.maps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
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
import com.righvalue.gmaptracker.AppConstants
import com.righvalue.gmaptracker.Constants
import com.righvalue.gmaptracker.R
import com.righvalue.gmaptracker.Tracker


class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var tracker: Tracker

    private var trackingState: Boolean = false
    private var backgroundTracking: Boolean = false
    private var isGPS = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        val button = root.findViewById<FloatingActionButton>(R.id.btn_start_location_updates)
        button.setOnClickListener { startLocationTracking() }

        tracker = Tracker(requireContext())
        initialize()
        return root
    }

    private fun initialize() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        GoogleService(requireContext()).turnGPSOn { isGPSEnable -> isGPS = isGPSEnable }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest()
        locationRequest.interval = Constants.UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.maxWaitTime = 15000

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onUpdateLocations(locationResult)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.e(Constants.TAG, "onMayReady")
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun checkPermission(permission: String) : Boolean {
        val result = ActivityCompat.checkSelfPermission(requireContext(), permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationTracking() {
        if (!trackingState) {
            Log.e(Constants.TAG, "startLocationTracking")
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                Log.e(Constants.TAG, "startLocationTracking-RequestPermissions")
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    AppConstants.LOCATION_REQUEST
                )
            } else {
                startLocationService()
            }
        }
        else {
            stopLocationUpdates()
        }
    }

    private fun startLocationService() {
        trackingState = true
        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

            Toast.makeText(
                requireContext(),
                "Started location tracking!",
                Toast.LENGTH_SHORT
            ).show()
        } catch (ex: SecurityException) {
            ex.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        trackingState = false
        mFusedLocationClient.removeLocationUpdates(locationCallback)

        Toast.makeText(
            requireContext(),
            "Stopped location tracking!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.e(Constants.TAG, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.LOCATION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(Constants.TAG, "onResume")
        backgroundTracking = false
    }

    override fun onPause() {
        super.onPause()
        Log.e(Constants.TAG, "onPause")
        backgroundTracking = true
    }

    private fun onUpdateLocations(result: LocationResult?) {
        Log.e(Constants.TAG, "onUpdateLocation")
        if (result != null) {
            for (location in result.locations) {
                if (location != null) {
                    onLocationChanged(location)
                }
            }
        }
    }

    private fun onLocationChanged(location: Location) {
        Log.e(Constants.TAG, "onLocationChanged: ${location.latitude}, ${location.longitude}")

        tracker.updateLocation(2, location.latitude, location.longitude)

        val latlng = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(latlng).title("Marker in india"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f))
    }
}