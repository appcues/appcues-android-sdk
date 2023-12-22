package com.appcues.samples.kotlin

import android.annotation.SuppressLint
import android.app.Notification.BigPictureStyle
import android.app.Notification.BigTextStyle
import android.app.Notification.Builder
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.URL

class PushNotificationService: FirebaseMessagingService() {

    @SuppressLint("MissingPermission")
    @RequiresApi(VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // we use `data` messages, not `notification`, since `notification` only triggers the app
        // code if the app is already in the foreground, and we need to be able to modify the look
        // and feel and support deep links, whether the app is foreground or background, here.

        val title = remoteMessage.data["title"]
        val text = remoteMessage.data["body"]

        if (title == null || text == null) return

        val image = remoteMessage.data["appcues_attachment_url"]
        val deepLink = remoteMessage.data["appcues_deep_link_url"]

        val bitmap: Bitmap? = image?.let {
            try {
                val url = URL(it)
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: IOException) {
                null
            }
        }

        val builder = Builder(this, ExampleApplication.pushChannel)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        if (bitmap != null) {
            builder.setStyle(
                BigPictureStyle().bigPicture(bitmap).setSummaryText(text)
            )
        } else {
            builder.setStyle(
                BigTextStyle().bigText(text)
            )
        }

        if (deepLink != null) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(deepLink))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
            builder.setContentIntent(pendingIntent)
        }

        NotificationManagerCompat.from(this).notify(1, builder.build())

        super.onMessageReceived(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        ExampleApplication.appcues.setPushToken(token)
    }
}