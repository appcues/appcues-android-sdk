package com.appcues.samples.kotlin

import android.annotation.SuppressLint
import android.app.Notification.Builder
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService: FirebaseMessagingService() {

    @SuppressLint("MissingPermission")
    @RequiresApi(VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title: String? = remoteMessage.notification?.title
        val text: String? = remoteMessage.notification?.body

        val notification: Builder = Builder(this, ExampleApplication.pushChannel)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(1, notification.build())

        super.onMessageReceived(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        ExampleApplication.appcues.setPushToken(token)
    }
}