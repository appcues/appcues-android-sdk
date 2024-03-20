package com.appcues.util

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.appcues.AppcuesFirebaseMessagingService.AppcuesMessagingData
import com.appcues.R
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal fun Context.getNotificationBuilder(
    channelId: String,
    channelName: String,
    channelDescription: String
): NotificationCompat.Builder {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        with(getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager) {
            createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = channelDescription }
            )
        }
    }

    return NotificationCompat.Builder(this, channelId)
}

internal fun NotificationCompat.Builder.setStyle(context: Context, data: AppcuesMessagingData) = apply {
    downloadImageFromUrl(data.imageUrl)?.let { image ->
        setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(image)
                .setSummaryText(data.body)
        )
    } ?: run {
        setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(data.body)
        )
    }

    // those should be custom icons and colors coming from customer side
    setSmallIcon(R.drawable.appcues_ic_white_logo)
    setColor(ContextCompat.getColor(context, android.R.color.background_dark))
}

@SuppressLint("QueryPermissionsNeeded")
internal fun NotificationCompat.Builder.setContentIntent(context: Context, data: AppcuesMessagingData) = apply {
    val deepLink = "appcues-${data.appId}://sdk/notification"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    intent.putExtra("notification_id", data.notificationId)
    intent.putExtra("forward_deeplink", data.deepLink)
    intent.putExtra("show_content", data.experienceId)
    setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE))
}

@SuppressLint("MissingPermission")
// Suppressing MissingPermission for POST_NOTIFICATION since this should be done on customer's end.
internal fun NotificationCompat.Builder.notify(notificationId: Int, context: Context) {
    NotificationManagerCompat.from(context).notify(notificationId, build())
}

@SuppressWarnings("TooGenericExceptionCaught", "SwallowedException")
// Suppressing both exception related warnings because all we want in this case is to return null
private fun downloadImageFromUrl(imageUrl: String?): Bitmap? {
    return try {
        val connection = URL(imageUrl).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val inputStream: InputStream = connection.inputStream
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        return null
    }
}
