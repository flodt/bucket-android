package de.schmidt.bucket.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.schmidt.bucket.R
import de.schmidt.bucket.activities.AllSetActivity

class Authentication {

    companion object {
        val currentUser get() = FirebaseAuth.getInstance().currentUser
        private const val signInRequestCode = 0xFF
        private var successfulCallback: (() -> Unit)? = null
        private var errorCallback: ((IdpResponse?) -> Unit)? = null

        fun initiateSignIn(context: Activity) {
            //we want google and e-mail as sign in methods
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            //launch sign-in
            context.startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setLogo(R.mipmap.ic_launcher)
                    .setAvailableProviders(providers)
                    .build(),
                signInRequestCode
            )
        }

        fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            //check if we collected our sign in
            if (requestCode == signInRequestCode) {
                if (resultCode == Activity.RESULT_OK) {
                    //sign-in was successful, invoke the callback
                    Log.d("SignIn", "Successful!")
                    successfulCallback?.invoke()
                } else {
                    Log.e("SignIn", "Not successful")
                    //sign in was not successful, error code?
                    val response = IdpResponse.fromResultIntent(data)
                    errorCallback?.invoke(response)
                }
            }
        }

        fun setOnSuccessfulListener(callback: () -> Unit) {
            successfulCallback = callback
        }

        fun setOnErrorListener(callback: (IdpResponse?) -> Unit) {
            errorCallback = callback
        }

        fun signOut(context: Activity, complete: () -> Unit = {
            context.runOnUiThread{ Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show() }
        }) {
            AuthUI.getInstance().signOut(context).addOnCompleteListener {
                //restart main activity after sign-out
                context.finish()
                context.startActivity(Intent(context, AllSetActivity::class.java))

                //call the passed listener
                complete()
            }
        }

        fun delete(context: Activity, complete: () -> Unit = {
            context.runOnUiThread{ Toast.makeText(context, "Deleted account", Toast.LENGTH_SHORT).show() }
        }) {
            //verify deletion
            AlertDialog.Builder(context)
                .setTitle("Delete account?")
                .setMessage("Do you really wish to delete your account?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes") { _, _ ->
                    AuthUI.getInstance().delete(context).addOnCompleteListener {
                        //restart main activity after sign-out
                        context.finish()
                        context.startActivity(Intent(context, AllSetActivity::class.java))

                        //call the passed listener
                        complete()
                    }
                }
                .create()
                .show()
        }

        fun setOnStateChangeListener(onStateChange: (FirebaseAuth) -> Unit) {
            FirebaseAuth.getInstance().addAuthStateListener(onStateChange)
        }
    }
}