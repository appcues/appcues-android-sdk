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
import com.google.firebase.messaging.RemoteMessage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// for now channels are not configurable, which means all push messages will come through the default channel defined in XML config
internal fun Context.getNotificationBuilder(): NotificationCompat.Builder {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        with(getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager) {

            createNotificationChannel(
                NotificationChannel(
                    getString(R.string.appcues_notification_channel_id),
                    getString(R.string.appcues_notification_channel_name),
                    resources.getInteger(R.integer.appcues_notification_channel_importance)
                ).apply { description = getString(R.string.appcues_notification_channel_description) }
            )
        }
    }

    return NotificationCompat.Builder(this, getString(R.string.appcues_notification_channel_id))
}

internal fun NotificationCompat.Builder.setupNotification(message: RemoteMessage) = apply {
    setAutoCancel(true)
    setPriority(message.priority)
    message.notification?.visibility?.let { setVisibility(it) }
    message.notification?.channelId?.let { setChannelId(it) }
}

internal fun NotificationCompat.Builder.setContent(data: AppcuesMessagingData) = apply {
    setContentTitle(data.title)
    setContentText(data.body)
}

internal fun NotificationCompat.Builder.setStyle(context: Context, data: AppcuesMessagingData) = apply {
    downloadImageFromUrl(data.image)?.let { image ->
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

    setSmallIcon(R.drawable.appcues_notification_small_icon)
    setColor(ContextCompat.getColor(context, R.color.appcues_notification_color))
}

internal fun NotificationCompat.Builder.setIntent(context: Context, data: AppcuesMessagingData) = apply {
    val deepLink = "appcues-${data.appId}://sdk/notification"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    intent.putExtra("notification_id", data.notificationId)
    intent.putExtra("forward_deeplink", data.deeplink)
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
