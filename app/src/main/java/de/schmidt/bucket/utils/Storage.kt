package de.schmidt.bucket.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.TextView
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

        @SuppressLint("SetTextI18n")
        fun downloadFilesAndThen(
            files: List<StorageReference>,
            context: Activity,
            progress: ProgressBar? = null,
            fileOfFiles: TextView? = null,
            executeOnLast: (downloaded: File, type: String?) -> Unit
        ) {
            //set initial progress
            fileOfFiles?.visibility = View.VISIBLE
            fileOfFiles?.text = "0/${files.size}"

            var count = 0

            files.forEach { ref ->
                //download each file
                downloadFileAndThen(ref, context, progress) { downloaded, type ->
                    Log.d("FirebaseStorage", "Received ${downloaded.absolutePath} with type $type")

                    //increment counter after success
                    synchronized(count) {
                        count++

                        //update secondary progress bar
                        fileOfFiles?.text = "${count}/${files.size}"
                        val secondary = 100.0 * (count.toDouble() / (files.size.toDouble()))
                        progress?.secondaryProgress = secondary.toInt()

                        //if last iteration: trigger callback
                        if (count == files.size) {
                            //trigger the callback with the last file info
                            executeOnLast(downloaded, type)
                        }
                    }
                }
            }
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

            Log.d(
                "FirebaseStorage",
                "Downloading ${storageRef.name} into ${localFile?.absolutePath} with storage ref extension $extension"
            )

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
                    progress?.let { progress ->
                        //update progress bar if present
                        context.runOnUiThread {
                            val current = 100 * ((it.bytesTransferred.toDouble()) / (it.totalByteCount.toDouble()))
                            context.runOnUiThread {
                                progress.visibility = View.VISIBLE
                                progress.progress = current.toInt()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    context.runOnUiThread { Toast.makeText(context, "Download error!", Toast.LENGTH_SHORT).show() }
                    Log.e("FirebaseStorage", "Download error", it)
                }
        }

        fun deleteFileAndThen(ref: StorageReference, context: Activity, execute: () -> Unit) {
            ref.delete()
                .addOnSuccessListener { execute() }
                .addOnFailureListener {
                    context.runOnUiThread { Toast.makeText(context, "File deletion error!", Toast.LENGTH_SHORT).show() }
                    Log.e("FirebaseStorage", "File delete error", it)
                }
        }

        fun deleteAllFilesAndThen(context: Activity, execute: () -> Unit) {
            listFilesAndThen { list ->
                //if the list is already empty, execute the callback
                if (list.isEmpty()) execute()

                //else clear all files, then execute the callback
                list.forEachIndexed { i, ref ->
                    deleteFileAndThen(ref, context) {
                        //execute the callback after the last deletion
                        if (i == list.size - 1) execute()
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun uploadFilesAndThen(
            uris: ArrayList<Uri?>,
            context: Activity,
            progress: ProgressBar? = null,
            fileOfFiles: TextView? = null,
            execute: () -> Unit
        ) {
            fileOfFiles?.text = "0/${uris.filterNotNull().size}"
            fileOfFiles?.visibility = View.VISIBLE

            var count = 0

            uris.filterNotNull().forEach { uri ->
                uploadFileAndThen(uri, context, progress) {
                    synchronized(count) {
                        //increment counter
                        count++

                        //set secondary progress between file uploads
                        fileOfFiles?.text = "${count}/${uris.filterNotNull().size}"
                        val secondary = 100.0 * (count.toDouble() / (uris.filterNotNull().size.toDouble()))
                        progress?.secondaryProgress = secondary.toInt()

                        //if we're at the last upload, execute the callback
                        if (count == uris.filterNotNull().size) {
                            execute()
                        }
                    }
                }
            }
        }

        fun uploadFileAndThen(
            uri: Uri,
            context: Activity,
            progress: ProgressBar? = null,
            execute: (StorageReference) -> Unit
        ) {
            //clear the bucket if it's not empty
            Log.d("FirebaseStorage", "Bucket cleared")

            //create reference for the uploaded file
            val ref = storage
                .reference
                .child("buckets")
                .child(Authentication.currentUser?.uid.toString())
                .child(uri.getFileName(context))

            Log.d("FirebaseStorage", "Uploading to reference ${ref.path}")

            //extract input stream
            context.contentResolver.openInputStream(uri)?.let { stream ->
                val size = uri.getFileSize(context)

                ref.putStream(stream)
                    .addOnSuccessListener {
                        Log.d("FirebaseStorage", "Upload successful")
                        execute(ref)
                    }
                    .addOnFailureListener {
                        context.runOnUiThread {
                            Toast.makeText(context, "File upload error!", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("FirebaseStorage", "File upload error", it)
                    }
                    .addOnProgressListener {
                        progress?.apply {
                            //update progress bar if present, set indeterminate if we don't know the size (-1)
                            isIndeterminate = size == -1L
                            val current = 100 * ((it.bytesTransferred.toDouble()) / (size.toDouble()))
                            Log.d(
                                "FirebaseStorage",
                                "Progress call ${it.bytesTransferred}/$size bytes, progress $current"
                            )
                            setProgress(current.toInt())
                        }
                    }
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

        fun Uri.getFileName(context: Activity): String {
            var result: String? = null

            //try the nice way
            if (scheme == "content") {
                context
                    .contentResolver
                    .query(this, null, null, null, null)
                    ?.use {
                        if (it.moveToFirst()) {
                            result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    }
            }

            //else we extract manually
            if (result == null) {
                result = path
                val lastDelimiter = result?.lastIndexOf('/')
                if (lastDelimiter != null && lastDelimiter != -1) {
                    result = result?.substring(lastDelimiter + 1)
                }
            }

            return result ?: ""
        }

        @SuppressLint("Recycle")
        fun Uri.getFileSize(context: Activity): Long {
            context
                .contentResolver
                .query(this, null, null, null, null)
                .let { it ?: return -1 }
                .use {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    it.moveToFirst()
                    return it.getLong(sizeIndex)
                }
        }
    }

}