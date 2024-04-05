package com.appcues

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.appcues.data.MoshiConfiguration
import com.appcues.util.getNotificationBuilder
import com.appcues.util.notify
import com.appcues.util.setContent
import com.appcues.util.setIntent
import com.appcues.util.setStyle
import com.appcues.util.setupNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Appcues Firebase Messaging Service implementation.
 *
 * To use it you can either add directly to your Manifest as a service or
 * call handleMessage and setToken on your custom implementation.
 */
public class AppcuesFirebaseMessagingService : FirebaseMessagingService() {

    public companion object {

        internal var notificationId = 1_000_000

        internal const val CHECK_PUSH_NOTIFICATION_ID = 1_100_100

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
                val appcuesData = message.data["appcues"] ?: throw IllegalStateException("Appcues message data not found.")
                MoshiConfiguration.moshi.adapter(AppcuesMessagingData::class.java).fromJson(appcuesData)!!
            } catch (_: Exception) {
                // unable to get data, most likely means this message is not for us.
                return false
            }

            // check for permission
            if (ActivityCompat.checkSelfPermission(context, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                return false

            val isCheckPush = data.test && data.notificationId.startsWith("test-push")

            context.getNotificationBuilder()
                .setupNotification(message)
                .setContent(data)
                .setStyle(context, data)
                .setIntent(context, data, isCheckPush)
                .notify(context, isCheckPush)

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

    @JsonClass(generateAdapter = true)
    internal data class AppcuesMessagingData(
        val title: String,
        val body: String,
        @Json(name = "notification_id")
        val notificationId: String,
        @Json(name = "notification_version")
        val notificationVersion: Long? = null,
        @Json(name = "account_id")
        val accountId: String,
        @Json(name = "app_id")
        val appId: String,
        @Json(name = "user_id")
        val userId: String,
        @Json(name = "workflow_id")
        val workflowId: String?,
        @Json(name = "workflow_task_id")
        val workflowTaskId: String?,
        @Json(name = "workflow_version")
        val workflowVersion: Long? = null,
        @Json(name = "deep_link_url")
        val deeplink: String?,
        @Json(name = "attachment_url")
        val image: String?,
        @Json(name = "experience_id")
        val experienceId: String?,
        @Json(name = "category")
        val category: String?,
        @Json(name = "android_notification_id")
        val androidNotificationId: String?,
        @Json(name = "test")
        val test: Boolean = false
    )

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(this, remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        setToken(token)
    }
}
