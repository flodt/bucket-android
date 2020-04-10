package de.schmidt.bucket.activities

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.Storage

class NewDocumentActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_new_document)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)

        emailLabel = findViewById(R.id.signed_in_info_newdocument)
        downloadButton = findViewById(R.id.download_button)
        progressBar = findViewById(R.id.download_progress_bar)

        downloadButton.setOnClickListener {
            //refresh to verify we have a file
            refresh()

            //initiate download of the first file in the list
            Storage.listFilesAndThen {
                //get first references
                val first = it[0]

                //start download
                Storage.downloadFileAndThen(first, this, progressBar) { file, type ->
                    //get the download manager service to hand over the downloaded file
                    val downloadManager: DownloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.addCompletedDownload(
                        file.name,
                        file.nameWithoutExtension,
                        true,
                        type,
                        file.absolutePath,
                        file.length(),
                        true
                    )
                }

                //todo delete downloaded file
            }
        }

        //todo add clear button to delete the files in the buffer
        //todo add activity to upload file
        //todo handle activity lifecycle changes in download
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
