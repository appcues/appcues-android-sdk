package com.appcues

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.appcues.util.getNotificationBuilder
import com.appcues.util.notify
import com.appcues.util.setContentIntent
import com.appcues.util.setStyle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Appcues Firebase Messaging Service implementation.
 *
 * To use it you can either add directly to your Manifest as a service or
 * call handleMessage and setToken on your custom implementation.
 */
public class AppcuesFirebaseMessagingService : FirebaseMessagingService() {

    public companion object {

        private var notificationId = 0

        /**
         * handleMessage will try to parse the received message into an Appcues notification, if it does it will return true,
         * or false in case the message is not for Appcues to handle
         *
         * @param context application context ('this' when calling from service)
         * @param message remote message coming from the messaging service
         *
         * @return determining whether we handled the message or not.
         */
        @JvmStatic
        public fun handleMessage(context: Context, message: RemoteMessage): Boolean {
            val data = try {
                AppcuesMessagingData(message)
            } catch (_: IllegalStateException) {
                return false
            }

            if (data.isTesting) {
                // during testing we just want to validate that push message came through
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("appcues-${data.appId}://sdk/debugger/push-token")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
                return true
            }

            // maybe this will be provided within the message somehow?
            val defaultChannelId = "Appcues"
            val defaultChannelName = "Appcues"
            val defaultChannelDescription = "Appcues default channel"
            // check for permission
            if (ActivityCompat.checkSelfPermission(context, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                return false

            context.getNotificationBuilder(defaultChannelId, defaultChannelName, defaultChannelDescription)
                .setContentTitle(data.title)
                .setContentText(data.body)
                .setAutoCancel(true)
                .setStyle(context, data)
                .setContentIntent(context, data)
                .notify(notificationId++, context)

            return true
        }

        /**
         * sets the global appcues token
         *
         * @param token token value coming from the messaging service
         */
        @JvmStatic
        public fun setToken(token: String) {
            Appcues.pushToken = token
        }
    }

    internal class AppcuesMessagingData(message: RemoteMessage) {

        // required
        val userId: String = message.data["appcues_user_id"] ?: throw PropertyNotFound("appcues_user_id")
        val accountId: String = message.data["appcues_account_id"] ?: throw PropertyNotFound("appcues_user_id")
        val appId: String = message.data["appcues_app_id"] ?: throw PropertyNotFound("appcues_app_id")
        val notificationId: String = message.data["appcues_notification_id"] ?: throw PropertyNotFound("appcues_notification_id")
        val title: String = message.data["title"] ?: throw PropertyNotFound("title")
        val body: String = message.data["body"] ?: throw PropertyNotFound("body")
        val isTesting: Boolean = message.data.containsKey("appcues_test")

        // optional values
        val imageUrl: String? = message.data["appcues_attachment_url"]
        val deepLink: String? = message.data["appcues_deep_link_url"]
        val experienceId: String? = message.data["appcues_experience_id"]

        private class PropertyNotFound(property: String) : IllegalStateException("AppcuesMessagingData: $property not found.")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(this, remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        setToken(token)
    }
}
