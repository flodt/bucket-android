package de.schmidt.bucket.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication

class AllSetActivity : BaseActivity() {
    private lateinit var label: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        label = findViewById(R.id.auth_user_textview)
    }

    override fun updateUI() {
        runOnUiThread { label.text = Authentication.getCurrentlyAuthenticatedUser()?.uid }
    }
}
