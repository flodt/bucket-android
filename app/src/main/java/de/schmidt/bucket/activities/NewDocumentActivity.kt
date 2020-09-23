package de.schmidt.bucket.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.*

class NewDocumentActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var pendingDocuments: TextView
    private lateinit var downloadButton: Button
    private lateinit var openURLButton: Button
    private lateinit var clearButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var fileOfFiles: TextView
    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_new_document)
    private var downloading = false
    private lateinit var handler: Handler
    private lateinit var refresher: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)

        emailLabel = findViewById(R.id.signed_in_info_newdocument)
        downloadButton = findViewById(R.id.download_button)
        openURLButton = findViewById(R.id.open_url_button)
        clearButton = findViewById(R.id.clear_button)
        progressBar = findViewById(R.id.download_progress_bar)
        fileOfFiles = findViewById(R.id.download_progress_info)
        pendingDocuments = findViewById(R.id.pending_documents_textview)

        clearButton.setOnClickListener {
            Database.submitURL("") {
                Storage.deleteAllFilesAndThen(this) {
                    Log.d("FirebaseStorage", "Deleted bucket contents")
                    refresh()
                }
            }
        }

        openURLButton.setOnClickListener {
            //refresh to verify we have a URL
            refresh()

            //retrieve and open the URL
            Database.retrieveURL(this) { databaseUrl ->
                val browseUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(databaseUrl))
                startActivity(browseUrlIntent)
            }
        }

        downloadButton.setOnClickListener {
            if (downloading) return@setOnClickListener
            downloading = true

            //refresh to verify we have a file
            refresh()

            //initiate download of the first file in the list
            Storage.listFilesAndThen { list ->
                val progressMgr = ProgressManager(list, this, progressBar)
                Storage.downloadFilesAndThen(list, this, progressMgr, fileOfFiles) { downloaded, type ->
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
                        val title = "${list.size} file" + (if (list.size > 1) "s" else "") + " downloaded"
                        val description = downloaded.name + (if (list.size > 1) " and ${list.size - 1} others…" else "")
                        NotificationUtils.fireDownloadedNotification(
                            this, title, description, it
                        )

                        //reset view
                        downloading = false
                        progressBar.visibility = View.INVISIBLE
                        fileOfFiles.visibility = View.INVISIBLE
                    }
                }
            }
        }

        handler = Handler()
        refresher = Runnable {
            refreshSilently()
            handler.postDelayed(refresher, 5_000L)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresher)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresher)
    }

    override fun refresh() {
        swipeRefresh?.isRefreshing = true
        refreshSilently()
    }

    private fun refreshSilently() {
        Log.d("NewDocumentActivity", "Silent refresh triggered…")
        //if we don't have any files in the bucket anymore, go back to the all set activity
        Storage.listFilesAndThen { list ->
            //check if there is no URL in the bucket
            Database.hasURL(this) { hasUrl ->
                //update the buttons
                downloadButton.visibility = if (list.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                openURLButton.visibility = if (hasUrl) View.VISIBLE else View.INVISIBLE

                //leave if necessary
                if (list.isEmpty() && !hasUrl) {
                    finish()
                    startActivity(Intent(this, AllSetActivity::class.java))
                }

                swipeRefresh?.isRefreshing = false
            }
        }
    }

    override fun updateUI() {
        runOnUiThread {
            emailLabel.text = "Signed in as ${Authentication.currentUser?.email}"
        }

        //set the number of pending documents
        Storage.listFilesAndThen {
            runOnUiThread {
                downloadButton.text = "Download ${it.size} files…"
            }
        }
    }
}
