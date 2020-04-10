package de.schmidt.bucket.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.MenuUtils
import de.schmidt.bucket.utils.NotificationUtils

abstract class BaseActivity: AppCompatActivity() {
    private val STORAGE_PERMISSION_REQUEST: Int = 0xFF0F
    protected abstract val swipeRefresh: SwipeRefreshLayout?

    abstract fun updateUI()
    abstract fun refresh()

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

        Authentication.setOnStateChangeListener { refresh() }
    }

    override fun onResume() {
        super.onResume()

        //register notification channel
        NotificationUtils.createNotificationChannel(this)

        //add refresh trigger to swipeRefreshLayout
        swipeRefresh?.setOnRefreshListener { refresh() }

        refresh()
        updateUI()

        //verify storage permission
        verifyStoragePermission()

        //trigger sign-in if not already signed in
        if (Authentication.currentUser == null) {
            Authentication.initiateSignIn(this)
        }
    }

    private fun verifyStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //permission is not yet granted, we need to ask for this
            //do we need to show rationale?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show explanation
                AlertDialog.Builder(this)
                    .setTitle("Storage permission")
                    .setMessage("Storage permission is needed for the file download to work.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes") { _, _ ->
                        //request permission after confirmation
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_PERMISSION_REQUEST
                        )
                    }
            } else {
                //don't need to explain
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST
                )
            }
        } else {
            Log.d("BaseActivity", "Storage permission was already granted.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            //array not empty, permission granted:
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("BaseActivity", "Storage permission was granted on request.")
            } else {
                //user did not grant the permission, but we need it, so ask again
                Log.e("BaseActivity", "Permission not granted.")
            }
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