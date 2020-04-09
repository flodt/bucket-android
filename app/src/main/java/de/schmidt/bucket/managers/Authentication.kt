package de.schmidt.bucket.managers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.schmidt.bucket.R

class Authentication {

    companion object {
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

        fun getCurrentlyAuthenticatedUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser
    }
}