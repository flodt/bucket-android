package de.schmidt.bucket.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.schmidt.bucket.R

class NotificationUtils {
    companion object {
        private const val CHANNEL_ID = "BUCKET_NETWORK"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("NotificationUtils", "Create notification channel...")
                //build channel
                val name = context.getString(R.string.channel_name)
                val description = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.description = description

                //register channel
                val manager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        fun fireDownloadedNotification(context: Context, title: String, description: String, launchIntent: Intent) {
            Log.d("NotificationUtils", "Fire downloaded notification with title $title and descr $description")

            //build the PendingIntent
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT
            )

            //construct notification
            NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setContentTitle(title)
                setContentText(description)
                setSmallIcon(R.drawable.ic_file_download)
                setChannelId(CHANNEL_ID)
                priority = NotificationCompat.PRIORITY_HIGH
                setLargeIcon(DrawableUtils.generateBitmap(context, R.mipmap.ic_launcher))
                color = context.getColor(R.color.colorPrimary)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }.let { builder ->
                //fire notification
                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            }
        }
    }
}