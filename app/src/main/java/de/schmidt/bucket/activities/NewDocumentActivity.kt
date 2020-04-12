package de.schmidt.bucket.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.NotificationUtils
import de.schmidt.bucket.utils.Storage

class NewDocumentActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var pendingDocuments: TextView
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
        pendingDocuments = findViewById(R.id.pending_documents_textview)

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
                Storage.downloadFilesAndThen(list, this, progressBar) { downloaded, type ->
                    //send notification to open the downloaded file
                    //open the downloaded file
                    Intent().apply {
                        setType(type)
                        data = FileProvider.getUriForFile(
                            this@NewDocumentActivity,
                            applicationContext.packageName + ".provider",
                            downloaded)
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }.let {
                        //pass to notification
                        val title = "${list.size} file(s) downloaded"
                        val description = "${downloaded.name} and ${list.size - 1} others…"
                        NotificationUtils.fireDownloadedNotification(
                            this, title, description, it
                        )

                        //reset view
                        downloading = false
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    override fun refresh() {
        swipeRefresh?.isRefreshing = true
        refreshSilently()
    }

    private fun refreshSilently() {
        Log.d("NewDocumentActivity", "Silent refresh triggered…")
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

        //set the number of pending documents
        Storage.listFilesAndThen {
            runOnUiThread { pendingDocuments.text = "${it.size} pending documents" }
        }
    }
}
