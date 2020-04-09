package de.schmidt.bucket.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class Storage {
    companion object {
        private val storage = Firebase.storage

        fun listFilesAndThen(execute: (List<StorageReference>) -> Unit) {
            //get the reference to the root
            val reference = storage.reference
                .child("buckets")
                .child(Authentication.currentUser?.uid.toString())

            //list all children and call callback
            reference.listAll()
                .addOnSuccessListener { execute(it.items.filterNotNull()) }
                .addOnFailureListener { Log.e("FirebaseStorage", "An error occurred during file listing.") }
        }
    }
}