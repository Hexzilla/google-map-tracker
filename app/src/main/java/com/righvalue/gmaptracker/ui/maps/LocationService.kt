package com.righvalue.gmaptracker.ui.maps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import java.text.DateFormat
import java.util.*

class LocationService : Service(), ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null
    private var timeCurrentLocation: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
        timeCurrentLocation = ""
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")

        buildGoogleApiClient()

        googleApiClient.connect()
        if (googleApiClient.isConnected) {
            startLocationUpdates()
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e(TAG, "onTaskRemoved")

        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest()
        locationRequest.interval = AppUtils.UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = AppUtils.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.maxWaitTime = 15000

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.e(TAG, "onLocationResult")
                onUpdateLocations(locationResult)
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        } catch (ex: SecurityException) {
            ex.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        stopLocationUpdates()
        googleApiClient.disconnect()
        super.onDestroy()
    }

    @Throws(SecurityException::class)
    override fun onConnected(connectionHint: Bundle?) {
        Log.i(ContentValues.TAG, "Connected to GoogleApiClient")
        startLocationUpdates()
    }

    override fun onConnectionSuspended(cause: Int) {
        googleApiClient.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.errorCode)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /*@SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient.lastLocation.addOnSuccessListener(applicationContext) { location ->
            if (location != null) {
                updateLocation(location.latitude, location.longitude)
            } else {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }*/

    private fun onUpdateLocations(result: LocationResult?) {
        if (result != null) {
            for (location in result.locations) {
                if (location != null) {
                    onLocationChanged(location)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.e(TAG, "onLocationChanged: $location.latitude, $location.longitude")
        currentLocation = location
        timeCurrentLocation = DateFormat.getTimeInstance().format(Date())
        sendLocationBroadcast(location)
    }

    private fun sendLocationBroadcast(location: Location?) {

    }
}