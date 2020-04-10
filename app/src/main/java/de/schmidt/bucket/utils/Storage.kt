package de.schmidt.bucket.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

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

        fun downloadFileAndThen(
            storageRef: StorageReference,
            context: Activity,
            progress: ProgressBar? = null,
            execute: (downloaded: File, type: String?) -> Unit
        ) {
            //create a file in the downloads folder for the download
            val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val localFile = downloadDirectory?.createInDirectory(storageRef.name)
            val extension = storageRef.name.substringAfter(".")

            Log.d("FirebaseStorage", "Downloading ${storageRef.name} into ${localFile?.absolutePath} with storage ref extension $extension")

            //check for file creation issues
            if (localFile == null) {
                context.runOnUiThread { Toast.makeText(context, "Error in storage access!", Toast.LENGTH_SHORT).show() }
                return
            }

            //initiate download
            storageRef
                .getFile(localFile)
                .addOnSuccessListener {
                    //execute the callback with the determined mime type
                    execute(
                        localFile,
                        MimeTypeMap
                            .getSingleton()
                            .getMimeTypeFromExtension(extension)
                    )
                }
                .addOnProgressListener {
                    if (progress != null) {
                        //update progress bar if present
                        val current = 100 * ((it.bytesTransferred.toDouble()) / (it.totalByteCount.toDouble()))
                        progress.visibility = View.VISIBLE
                        progress.progress = current.toInt()
                    }
                }
                .addOnFailureListener {
                    context.runOnUiThread { Toast.makeText(context, "Download error!", Toast.LENGTH_SHORT).show() }
                    Log.e("FirebaseStorage", "Download error", it)
                }
        }

        fun File.createInDirectory(nameWithExtension: String): File {
            //initialize file
            var local = File(this, nameWithExtension)

            //if the local file does not exist yet, create it
            if (!local.exists()) {
                local.createNewFile()
            } else {
                //if it does exist, do not overwrite, but rather append current time
                val uniqueName = nameWithExtension.substringBefore(".") +
                        " " + System.currentTimeMillis() +
                        "." + nameWithExtension.substringAfter(".")
                local = File(this, uniqueName)
                local.createNewFile()
            }

            Log.d("FirebaseStorage", "Created file for download as ${local.absolutePath}")

            return local
        }
    }

}