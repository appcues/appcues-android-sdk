package com.appcues

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
            // check for permission
            if (ActivityCompat.checkSelfPermission(context, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                return false

            val data = try {
                AppcuesMessagingData(message)
            } catch (_: IllegalStateException) {
                return false
            }

            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

            NotificationCompat.Builder(context, "Appcues")
                .setContentTitle(data.title)
                .setContentText(data.body)
                .setAutoCancel(true)
                .setSmallIcon(appInfo.icon) // Not working as intended
                .setStyle(data)
                .setContentIntent(context, data)
                .notify(1, context)

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
        val title: String = message.data["title"] ?: throw IllegalStateException("title not found")
        val body: String = message.data["body"] ?: throw IllegalStateException("body not found")
        val userId: String = message.data["appcues_user_id"] ?: throw IllegalStateException("appcues_user_id not found")
        val notificationId: String =
            message.data["appcues_notification_id"] ?: throw IllegalStateException("appcues_notification_id not found")

        // optional values
        val imageUrl: String? = message.data["appcues_attachment_url"]
        val deepLink: String? = message.data["appcues_deep_link_url"]
        val experienceId: String? = message.data["appcues_experience_id"]
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
