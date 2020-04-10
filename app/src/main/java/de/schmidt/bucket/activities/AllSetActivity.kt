package de.schmidt.bucket.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.Storage
import kotlinx.android.synthetic.main.activity_all_set.*

class AllSetActivity : BaseActivity() {
    private lateinit var emailLabel: TextView
    private lateinit var infoLabel: TextView
    private lateinit var icon: ImageView
    private lateinit var titleText: TextView

    override val swipeRefresh: SwipeRefreshLayout?
        get() = findViewById(R.id.pull_to_refresh_all_set)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_set)

        emailLabel = findViewById(R.id.signed_in_info)
        icon = findViewById(R.id.image_ok)
        titleText = findViewById(R.id.all_set_textview)
        infoLabel = findViewById(R.id.upload_file_info)

        //for debugging purposes: show UID on longpress
        emailLabel.isLongClickable = true
        emailLabel.setOnLongClickListener {
            Toast.makeText(this, Authentication.currentUser?.uid, Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
    }

    override fun refresh() {
        Log.d("NewDocumentActivity", "Refresh triggered…")
        swipeRefresh?.isRefreshing = true

        //list files
        Storage.listFilesAndThen { list ->
            list.forEach {
                Log.d("StorageResult", it.path)
            }

            //if we have pending files, jump to the new document activity, otherwise do nothing
            if (list.isNotEmpty()) {
                Log.d("StorageResult", "Non-empty, showing document to user…")
                finish()
                startActivity(Intent(this, NewDocumentActivity::class.java))
            }

            swipeRefresh?.isRefreshing = false
        }
    }

    override fun updateUI() {
        runOnUiThread {
            //handle signed in case
            if (Authentication.currentUser != null) {
                //set the label info for sign-in
                emailLabel.visibility = View.VISIBLE
                emailLabel.text = "Signed in as ${Authentication.currentUser?.email}"

                //show the text info
                infoLabel.visibility = View.VISIBLE

                //set icon to all set
                icon.setImageDrawable(getDrawable(R.drawable.ic_ok))

                //set title text to all set
                titleText.text = "You're all set."
                titleText.setTextColor(getColor(R.color.greenAccent))
            } else {
                //hide sign-in infos
                emailLabel.visibility = View.INVISIBLE
                infoLabel.visibility = View.INVISIBLE

                //set icon to error
                icon.setImageDrawable(getDrawable(R.drawable.ic_err))

                //set title text to all set
                titleText.text = "Sign in…"
                titleText.setTextColor(getColor(R.color.greyAccent))
            }
        }
    }
}
