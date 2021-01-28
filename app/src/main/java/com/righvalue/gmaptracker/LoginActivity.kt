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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var tracker: Tracker
    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var loginLayout: LinearLayout
    private lateinit var progressBar: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tracker = Tracker(this)

        // Address the email and password field
        emailEditText = findViewById(R.id.username)
        passEditText = findViewById(R.id.password)
        loginLayout = findViewById(R.id.loginLayout)
        progressBar = findViewById(R.id.loadingPanel)
    }

    fun checkLogin(v: View?) {
        val email = emailEditText.text.toString()
        if (!isValidEmail(email)) {
            emailEditText.error = "Please input user name"
        }

        val pass = passEditText.text.toString()
        if (!isValidPassword(pass)) {
            passEditText.error = "Password must be more than 8 characters"
        }
        if (!isValidEmail(email) || !isValidPassword(pass)) {
            return
        }

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
        return (email != null && email.length >= 4)
    }

    // validating password
    private fun isValidPassword(pass: String?): Boolean {
        return (pass != null && pass.length >= 4)
    }

    private fun login(username: String, password: String) {
        tracker.login(username, password) { error, response ->
            if (error == "success") {
                if (response != null && response["success"] == true) {
                    startMapActivity()
                }
                else {
                    hideProgressbar()
                    Toast.makeText(applicationContext, "Incorrect username or password!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                hideProgressbar()
                Toast.makeText(applicationContext, "Network Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMapActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    fun register(v: View?) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}