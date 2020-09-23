package de.schmidt.bucket.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class Database {
    companion object {
        private val database = Firebase.database

        fun retrieveURL(context: Activity, callback: (String) -> Unit) {
            database
                .reference
                .child(Authentication.currentUser?.uid.toString())
                .child("url")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val databaseUrl = snapshot.getValue<String>()
                        callback(databaseUrl ?: "")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //failed to submit URL
                        context.runOnUiThread {
                            Toast.makeText(context, "Error in getting URL", Toast.LENGTH_SHORT).show()
                            Log.e("Database", error.toString())
                        }
                    }
                })
        }

        fun submitURL(enterURL: String, callback: () -> Unit) {
            database
                .reference
                .child(Authentication.currentUser?.uid.toString())
                .child("url")
                .setValue(enterURL)
                .addOnSuccessListener {
                    callback()
                }
        }

        fun hasURL(context: Activity, callback: (Boolean) -> Unit) {
            retrieveURL(context) { callback(it != "") }
        }
    }
}