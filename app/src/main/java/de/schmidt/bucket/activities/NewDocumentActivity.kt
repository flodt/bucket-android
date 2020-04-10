package de.schmidt.bucket.activities

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.NotificationUtils
import de.schmidt.bucket.utils.Storage

class NewDocumentActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var downloadButton: Button
    private lateinit var clearButton: Button
    private lateinit var progressBar: ProgressBar
    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_new_document)
    private var downloading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)

        emailLabel = findViewById(R.id.signed_in_info_newdocument)
        downloadButton = findViewById(R.id.download_button)
        clearButton = findViewById(R.id.clear_button)
        progressBar = findViewById(R.id.download_progress_bar)

        clearButton.setOnClickListener {
            Storage.deleteAllFilesAndThen(this) {
                Log.d("FirebaseStorage", "Deleted bucket contents")
                refresh()
            }
        }

        downloadButton.setOnClickListener {
            if (downloading) return@setOnClickListener
            downloading = true

            //refresh to verify we have a file
            refresh()

            //initiate download of the first file in the list
            Storage.listFilesAndThen { list ->
                //get first references
                val first = list[0]

                //start download
                Storage.downloadFileAndThen(first, this, progressBar) { file, type ->
                    Log.d("FirebaseStorage", "Received ${file.absolutePath} with type $type")

                    //send notification to open the downloaded file
                    //open the downloaded file
                    Intent().apply {
                        setType(type)
                        data = FileProvider.getUriForFile(
                            this@NewDocumentActivity,
                            applicationContext.packageName + ".provider",
                            file)
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }.let {
                        /*try {
                            startActivity(it)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this, "There's no program to open this file", Toast.LENGTH_LONG).show()
                        }*/

                        //pass to notification
                        NotificationUtils.fireDownloadedNotification(
                            this, "File downloaded", file.name, it
                        )
                    }

                    //delete the downloaded file from the bucket
                    Storage.deleteFileAndThen(first, this) {
                        downloading = false
                        progressBar.visibility = View.INVISIBLE
                        refresh()
                    }
                    //downloading = false
                    //progressBar.visibility = View.INVISIBLE
                }

            }
        }
    }

    override fun refresh() {
        Log.d("NewDocumentActivity", "Refresh triggered…")
        swipeRefresh?.isRefreshing = true

        //if we don't have any files in the bucket anymore, go back to the all set activity
        Storage.listFilesAndThen {
            if (it.isEmpty()) {
                finish()
                startActivity(Intent(this, AllSetActivity::class.java))
            }

            swipeRefresh?.isRefreshing = false
        }
    }

    override fun updateUI() {
        runOnUiThread {
            emailLabel.text = "Signed in as ${Authentication.currentUser?.email}"
        }
    }
}
