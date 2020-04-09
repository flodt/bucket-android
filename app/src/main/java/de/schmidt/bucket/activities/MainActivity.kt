package de.schmidt.bucket.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.schmidt.bucket.R
import de.schmidt.bucket.managers.Authentication

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var label: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.sign_in_button)
        label = findViewById(R.id.auth_user_textview)

        button.setOnClickListener {
            Authentication.initiateSignIn(this)
        }

        Authentication.setOnSuccessfulListener {
            runOnUiThread {
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }

        Authentication.setOnErrorListener {
            runOnUiThread {
                Toast.makeText(this, "Error in sign-in!", Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        label.text = Authentication.getCurrentlyAuthenticatedUser()?.uid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Authentication.handleOnActivityResult(requestCode, resultCode, data)
    }
}
