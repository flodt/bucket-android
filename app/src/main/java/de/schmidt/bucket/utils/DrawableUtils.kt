package de.schmidt.bucket.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

class DrawableUtils {
    companion object {
        fun generateBitmap(context: Context, @DrawableRes id: Int): Bitmap? {
            val drawable = ContextCompat.getDrawable(context, id)
            drawable ?: return null
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}