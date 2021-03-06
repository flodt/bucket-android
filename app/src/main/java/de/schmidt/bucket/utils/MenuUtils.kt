package de.schmidt.bucket.utils

import android.app.Activity
import android.view.MenuItem
import de.schmidt.bucket.R
import de.schmidt.bucket.activities.BaseActivity
import android.view.Menu as AndroidMenu


class MenuUtils {
    companion object Inflater {
        fun inflate(menu: AndroidMenu, context: Activity) {
            context.menuInflater.inflate(R.menu.app_menu, menu)
        }

        fun dispatch(item: MenuItem, context: BaseActivity) {
            when (item.itemId) {
                R.id.refresh_button -> context.refresh()
                R.id.delete_account_button -> Authentication.delete(context)
                R.id.sign_out_button -> Authentication.signOut(context)
            }
        }
    }
}