package de.schmidt.bucket.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.Storage

class UploadActivity : BaseActivity() {
    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_upload)
    private lateinit var progress: ProgressBar
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        progress = findViewById(R.id.upload_progress_bar)
        info = findViewById(R.id.signed_in_info_upload)

        //extract the file that was shared to this activity
        if (intent?.action == Intent.ACTION_SEND) {
            //get the data stream bundle
            val uri: Uri? = intent?.getParcelableExtra(Intent.EXTRA_STREAM)
            uri ?: return

            Log.d("UploadActivity", "Uploading URI $uri")

            //clear the bucket
            Storage.deleteAllFilesAndThen(this) {
                //now upload the file
                Storage.uploadFileAndThen(uri, this, progress) {
                    Toast.makeText(this, "File upload successful", Toast.LENGTH_SHORT).show()

                    //start the new document activity, file was uploaded
                    startActivity(Intent(this, NewDocumentActivity::class.java))
                }
            }
        } else if (intent?.action == Intent.ACTION_SEND_MULTIPLE) {
            //get the list of uris from the intent
            val uris = intent?.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            uris ?: return

            Log.d("UploadActivity", "Uploading URIs $uris")

            Storage.deleteAllFilesAndThen(this) {
                Storage.uploadFilesAndThen(uris, this, progress) {
                    Toast.makeText(this, "File uploads successful", Toast.LENGTH_SHORT).show()

                    //start the new document activity, file was uploaded
                    startActivity(Intent(this, NewDocumentActivity::class.java))
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
