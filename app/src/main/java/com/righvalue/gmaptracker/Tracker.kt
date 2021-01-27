package com.righvalue.gmaptracker

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Tracker {
    private lateinit var context: Context
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    constructor(context: Context) {
        this.context = context
    }

    fun updateLocation(userId: Int, latitude: Double, longitude: Double) {
        if (this.latitude != latitude || this.longitude != longitude) {
            this.latitude = latitude
            this.longitude = longitude

            val postUrl = "http://10.10.11.85:8000/api/location/update"
            val requestQueue = Volley.newRequestQueue(context)

            val postData = JSONObject()
            postData.put("user_id", userId)
            postData.put("lat", latitude)
            postData.put("long", longitude)

            val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, postUrl, postData,
                    Response.Listener<JSONObject> { response ->
                        Log.e("Tracker", response.toString())
                    },
                    Response.ErrorListener { error ->
                        error.printStackTrace()
                    })

            requestQueue.add(jsonObjectRequest)
        }
    }
}