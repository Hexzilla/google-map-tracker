package com.righvalue.gmaptracker.home

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.righvalue.gmaptracker.Constants
import com.righvalue.gmaptracker.R
import com.righvalue.gmaptracker.Tracker
import org.json.JSONArray
import org.json.JSONObject


class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tracker: Tracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        tracker = Tracker(requireContext())
        initialize()
        return root
    }

    private fun initialize() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_history) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        getTrackers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.e(ContentValues.TAG, "onMayReady")
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getTrackers() {
        tracker.getTrackers(2) { error, response ->
            if (error == "success" && response != null) {
                Log.d("DTracker", response.toString())
                updateRoutes(response["routes"] as JSONArray)
            }
            else {
                Log.e("DTracker", error)
            }
        }
    }

    private fun getLatLng(pos: JSONObject): LatLng? {
        if (pos["lat"] == null || pos["lng"] == null) {
            return null
        }
        return LatLng(pos["lat"].toString().toDouble(), pos["lng"].toString().toDouble())
    }

    private fun updateRoutes(routes: JSONArray) {
        Log.d("DTracker", routes.toString())

        if (routes.length() > 0) {
            val location = routes.getJSONObject(0)
            val point = getLatLng(location)
            if (point != null) {
                val options = MarkerOptions()
                options.position(point)
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                options.title("Start Position")
                mMap.addMarker(options)
            }
        }
        if (routes.length() > 1) {
            val location = routes.getJSONObject(routes.length() - 1)
            val point = getLatLng(location)
            if (point != null) {
                val options = MarkerOptions()
                options.position(point)
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                options.title("Last Position")
                mMap.addMarker(options)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            }
        }

        val lineOptions = PolylineOptions()
        for (i in 0 until routes.length()) {
            val item = routes.getJSONObject(i)
            val point = getLatLng(item)
            if (point != null) {
                lineOptions.add(point)
            }
        }

        lineOptions.width(12.0f)
        lineOptions.color(Color.RED)
        mMap.addPolyline(lineOptions);
    }
}