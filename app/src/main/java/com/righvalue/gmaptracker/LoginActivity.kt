package com.righvalue.gmaptracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Matcher
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Address the email and password field
        emailEditText = findViewById(R.id.username);
        passEditText = findViewById(R.id.password);
    }

    fun checkLogin(arg0: View?) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)

        /*val email = emailEditText.text.toString()
        if (!isValidEmail(email)) {
            emailEditText.error = "Invalid Email"
        }

        val pass = passEditText.text.toString()
        if (!isValidPassword(pass)) {
            passEditText.error = "Password cannot be empty"
        }
        if (isValidEmail(email) && isValidPassword(pass)) {

        }*/
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
}