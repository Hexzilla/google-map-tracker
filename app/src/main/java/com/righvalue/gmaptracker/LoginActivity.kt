package com.righvalue.gmaptracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var loginLayout: LinearLayout
    private lateinit var progressBar: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Address the email and password field
        emailEditText = findViewById(R.id.username)
        passEditText = findViewById(R.id.password)
        loginLayout = findViewById(R.id.login)
        progressBar = findViewById(R.id.loadingPanel)
    }

    fun checkLogin(arg0: View?) {
        val email = emailEditText.text.toString()
        /*if (!isValidEmail(email)) {
            emailEditText.error = "Invalid Email"
        }*/

        val pass = passEditText.text.toString()
        /*if (!isValidPassword(pass)) {
            passEditText.error = "Password cannot be empty"
        }
        if (!isValidEmail(email) || !isValidPassword(pass)) {
            return
        }*/

        showProgressbar()
        login(email, pass)
    }

    private fun showProgressbar() {
        for (i in 0 until loginLayout.childCount) {
            val view: View = loginLayout.getChildAt(i)
            view.isEnabled = false
        }
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        for (i in 0 until loginLayout.childCount) {
            val view: View = loginLayout.getChildAt(i)
            view.isEnabled = true
        }
        progressBar.visibility = View.INVISIBLE
    }

    // validating email id
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    // validating password
    private fun isValidPassword(pass: String?): Boolean {
        return (pass != null && pass.length >= 4)
    }

    private fun login(username: String, password: String) {
        val postUrl = "http://10.10.11.85:8000/api/login"
        val requestQueue = Volley.newRequestQueue(this)

        val postData = JSONObject()
        postData.put("name", username)
        postData.put("password", password)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, postUrl, postData,
            Response.Listener<JSONObject> { response ->
                hideProgressbar()
                Log.e("Tracker", response.toString())

                if (response["success"] == true) {
                    startMapActivity()
                }
                else {
                    Toast.makeText(applicationContext, "Incorrect username or password!", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                hideProgressbar()
                Toast.makeText(applicationContext, "Network Error!", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}