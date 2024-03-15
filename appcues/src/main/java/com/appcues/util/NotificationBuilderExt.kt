package com.appcues.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.appcues.AppcuesFirebaseMessagingService.AppcuesMessagingData
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal fun NotificationCompat.Builder.setStyle(data: AppcuesMessagingData) = apply {
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
}

internal fun NotificationCompat.Builder.setContentIntent(context: Context, data: AppcuesMessagingData) = apply {
    val intent = Intent().apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        setAction(Intent.ACTION_VIEW)

        if (data.deepLink != null) {
            setData(Uri.parse(data.deepLink))
        }
    }

    // Need to get valid launch activity and create intent from that.
    setContentIntent(
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
    )
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
