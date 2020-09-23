package de.schmidt.bucket.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.Database
import de.schmidt.bucket.utils.ProgressManager
import de.schmidt.bucket.utils.Storage

class UploadActivity : BaseActivity() {
    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_upload)
    private lateinit var progress: ProgressBar
    private lateinit var fileOfFiles: TextView
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        progress = findViewById(R.id.upload_progress_bar)
        info = findViewById(R.id.signed_in_info_upload)
        fileOfFiles = findViewById(R.id.upload_progress_info)

        //extract the file that was shared to this activity
        if (intent?.action == Intent.ACTION_SEND) {
            if (intent?.hasExtra(Intent.EXTRA_STREAM) == true) {
                //get the data stream bundle
                intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    Log.d("UploadActivity", "Uploading URI $uri")
                    Toast.makeText(this, "Uploading file...", Toast.LENGTH_SHORT).show()

                    //clear the bucket
                    Storage.deleteAllFilesAndThen(this) {
                        //now upload the file
                        val progressMgr = ProgressManager(listOf(uri), this, progress)
                        Storage.uploadFileAndThen(uri, this, progressMgr) {
                            Toast.makeText(this, "File upload successful", Toast.LENGTH_SHORT).show()

                            //start the new document activity, file was uploaded
                            startActivity(Intent(this, NewDocumentActivity::class.java))
                        }
                    }
                }
            } else if (intent?.hasExtra(Intent.EXTRA_TEXT) == true) {
                //no data stream, maybe we have text?
                val receivedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""

                if (URLUtil.isValidUrl(receivedText)) {
                    Toast.makeText(this, "Submitting URL...", Toast.LENGTH_SHORT).show()
                    Log.d("UploadActivity", "Discovered and uploading URL $receivedText...")
                    Database.submitURL(receivedText) {
                        Toast.makeText(this, "URL submission successful", Toast.LENGTH_SHORT).show()

                        //start the new document activity, URL was set
                        finish()
                        startActivity(Intent(this, NewDocumentActivity::class.java))
                    }
                } else {
                    //unable to handle this
                    finish()
                }
            } else {
                //also unable to handle this
                finish()
            }
        } else if (intent?.action == Intent.ACTION_SEND_MULTIPLE) {
            //get the list of uris from the intent
            intent?.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                Log.d("UploadActivity", "Uploading URIs $uris")

                Storage.deleteAllFilesAndThen(this) {
                    val progressMgr = ProgressManager(uris.filterNotNull(), this, progress)
                    Storage.uploadFilesAndThen(uris, this, progressMgr, fileOfFiles) {
                        Toast.makeText(this, "File uploads successful", Toast.LENGTH_SHORT).show()

                        //start the new document activity, file was uploaded
                        finish()
                        startActivity(Intent(this, NewDocumentActivity::class.java))
                    }
                }
            }
        }
    }

    override fun updateUI() {
        info.text = "Signed in as ${Authentication.currentUser?.email}"
    }

    override fun refresh() {
        updateUI()
    }
}
