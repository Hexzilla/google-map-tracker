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

    fun login(username: String, password: String, callback: (String, JSONObject?) -> Unit) {
        val postUrl = "${Constants.SERVER_URL}/api/login"
        val requestQueue = Volley.newRequestQueue(this.context)

        val postData = JSONObject()
        postData.put("name", username)
        postData.put("password", password)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, postUrl, postData,
            Response.Listener<JSONObject> { response ->
                Log.e("Tracker", response.toString())
                callback("success", response)
            },
            Response.ErrorListener { error ->
                callback(error.toString(), null)
            })

        requestQueue.add(jsonObjectRequest)
    }

    fun register(username: String, password: String, callback: (String, JSONObject?) -> Unit) {
        val postUrl = "${Constants.SERVER_URL}/api/register"
        val requestQueue = Volley.newRequestQueue(this.context)

        val postData = JSONObject()
        postData.put("name", username)
        postData.put("password", password)
        postData.put("password_confirmation", password)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, postUrl, postData,
            Response.Listener<JSONObject> { response ->
                Log.e("Tracker", response.toString())
                callback("success", response)
            },
            Response.ErrorListener { error ->
                callback(error.toString(), null)
            })

        requestQueue.add(jsonObjectRequest)
    }

    fun updateLocation(userId: Int, latitude: Double, longitude: Double) {
        if (this.latitude != latitude || this.longitude != longitude) {
            this.latitude = latitude
            this.longitude = longitude

            val postUrl = "${Constants.SERVER_URL}/api/location/update"
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

    fun getTrackers(userId: Int, callback: (String, JSONObject?) -> Unit) {
        val postUrl = "${Constants.SERVER_URL}/api/location/tracks"
        val requestQueue = Volley.newRequestQueue(context)

        val postData = JSONObject()
        postData.put("user_id", userId)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, postUrl, postData,
            Response.Listener<JSONObject> { response ->
                Log.e("Tracker", response.toString())
                callback("success", response)
            },
            Response.ErrorListener { error ->
                callback(error.toString(), null)
            })

        requestQueue.add(jsonObjectRequest)
    }
}