package de.schmidt.bucket.utils

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.activities.BaseActivity

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

            //toast the URI
            Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()
        }
    }

    override fun updateUI() {
        info.text = "Signed in as ${Authentication.currentUser?.email}"
    }

    override fun refresh() {
        updateUI()
    }
}
