package de.schmidt.bucket.utils

import android.app.Activity
import android.view.View
import android.widget.ProgressBar

class ProgressManager<T>(
    private val files: List<T>,
    private val context: Activity,
    private val progress: ProgressBar? = null,
    private val map: MutableMap<T, Int> = files.associateWith { 0 }.toMutableMap()
) {
    private fun getNormalizedProgress(): Int = map.values.sum() / map.values.size

    fun update(ref: T, newNormalizedValue: Int) {
        map[ref] = newNormalizedValue
        refreshProgressBar()
    }

    fun setIndeterminate(indeterminate: Boolean) {
        progress?.isIndeterminate = indeterminate
    }

    private fun refreshProgressBar() {
        progress?.let { progress ->
            //update progress bar if present
            context.runOnUiThread {
                progress.visibility = View.VISIBLE
                progress.progress = getNormalizedProgress()
            }
        }
    }
}