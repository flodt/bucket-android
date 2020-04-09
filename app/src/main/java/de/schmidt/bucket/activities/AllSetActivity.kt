package de.schmidt.bucket.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import de.schmidt.bucket.R
import de.schmidt.bucket.utils.Authentication
import de.schmidt.bucket.utils.Storage

class AllSetActivity : BaseActivity() {
    private lateinit var emailLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_set)

        emailLabel = findViewById(R.id.signed_in_info)

        //for debugging purposes: show UID on longpress
        emailLabel.isLongClickable = true
        emailLabel.setOnLongClickListener {
            Toast.makeText(this, Authentication.currentUser?.uid, Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        //for debugging purposes: jump to new document on longpress
        val ok: ImageView = findViewById(R.id.image_ok)
        ok.isLongClickable = true
        ok.setOnLongClickListener {
            startActivity(Intent(this, NewDocumentActivity::class.java))
            return@setOnLongClickListener true
        }
    }

    override fun updateUI() {
        runOnUiThread {
            //set the label info for sign-in
            emailLabel.text = "Signed in as ${Authentication.currentUser?.email}"
        }

        //list files
        Storage.listFilesAndThen { list ->
            list.forEach {
                Log.d("StorageResult", it.path)
            }
        }

        //now take these file objects and download them with the other activity!
    }
}
