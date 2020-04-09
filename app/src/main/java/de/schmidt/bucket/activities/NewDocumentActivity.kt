package de.schmidt.bucket.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication

class NewDocumentActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_document)

        emailLabel = findViewById(R.id.signed_in_info_newdocument)
        downloadButton = findViewById(R.id.download_button)
        progressBar = findViewById(R.id.download_progress_bar)

        downloadButton.setOnClickListener {
            //todo initiate download here
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 50 //only for visuals right now
        }
    }

    override fun updateUI() {
        runOnUiThread {
            emailLabel.text = "Signed in as ${Authentication.currentUser?.email}"
        }
    }
}
