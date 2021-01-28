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


class RegisterActivity : AppCompatActivity() {

    private lateinit var tracker: Tracker
    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var passConfirmEditText: EditText
    private lateinit var loginLayout: LinearLayout
    private lateinit var progressBar: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tracker = Tracker(this)

        // Address the email and password field
        emailEditText = findViewById(R.id.username)
        passEditText = findViewById(R.id.password)
        passConfirmEditText = findViewById(R.id.confirm_password)
        loginLayout = findViewById(R.id.loginLayout)
        progressBar = findViewById(R.id.loadingPanel)
    }

    fun checkRegister(v: View?) {
        val email = emailEditText.text.toString()
        if (!isValidEmail(email)) {
            emailEditText.error = "Please input user name"
        }

        val pass = passEditText.text.toString()
        if (!isValidPassword(pass)) {
            passEditText.error = "Password must be more than 8 characters"
        }

        val passConfirm = passConfirmEditText.text.toString()
        if (pass != passConfirm) {
            passConfirmEditText.error = "The password confirmation does not match"
            return
        }
        if (!isValidPassword(passConfirm)) {
            passConfirmEditText.error = "Password cannot be empty or must more than 8 characters"
        }
        if (!isValidEmail(email) || !isValidPassword(pass) || !isValidPassword(passConfirm)) {
            return
        }

        showProgressbar()
        register(email, pass)
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
        return (email != null && email.length >= 4)
    }

    // validating password
    private fun isValidPassword(pass: String?): Boolean {
        return (pass != null && pass.length >= 8)
    }

    private fun register(username: String, password: String) {
        tracker.register(username, password) { error, response ->
            if (error == "success") {
                if (response != null && response["success"] == true) {
                    startLoginActivity()
                }
                else {
                    hideProgressbar()
                    Toast.makeText(applicationContext, "Register Error!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Log.e("DTracker", error!!)
                hideProgressbar()
                Toast.makeText(applicationContext, "Network Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}