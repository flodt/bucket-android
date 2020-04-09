package de.schmidt.bucket.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.MenuUtils

abstract class BaseActivity: AppCompatActivity() {
    abstract fun updateUI()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

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

        Authentication.setOnStateChangeListener { updateUI() }
    }

    override fun onResume() {
        super.onResume()
        updateUI()

        //trigger sign-in if not already signed in
        if (Authentication.currentUser == null) {
            Authentication.initiateSignIn(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Authentication.handleOnActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) MenuUtils.inflate(menu, this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        MenuUtils.dispatch(item, this)
        return super.onOptionsItemSelected(item)
    }
}