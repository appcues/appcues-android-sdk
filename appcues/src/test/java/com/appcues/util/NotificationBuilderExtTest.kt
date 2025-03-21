package com.appcues.util

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.appcues.AppcuesFirebaseMessagingService.AppcuesMessagingData
import com.appcues.DeepLinkHandler
import com.appcues.push.PushDeeplinkHandler
import com.google.firebase.messaging.RemoteMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.Test

internal class NotificationBuilderExtTest {

    private val notificationBuilder = mockk<NotificationCompat.Builder>(relaxed = true)

    @Test
    fun `setupContent SHOULD set title and content`() {
        // Given
        val data = mockk<AppcuesMessagingData>().apply {
            every { title } returns "title1"
            every { body } returns "content body"
        }
        // When
        notificationBuilder.setContent(data)
        // Then
        verify { notificationBuilder.setContentTitle("title1") }
        verify { notificationBuilder.setContentText("content body") }
    }

    @Test
    fun `setupNotification SHOULD set autoCancel and priority`() {
        // Given
        val data = mockk<RemoteMessage>(relaxed = true).apply {
            every { priority } returns 5
        }
        // When
        notificationBuilder.setupNotification(data)
        // Then
        verify { notificationBuilder.setAutoCancel(true) }
        verify { notificationBuilder.setPriority(5) }
    }

    @Test
    fun `setupNotification SHOULD set visibility and channel id WHEN notification is present`() {
        // Given
        val data = mockk<RemoteMessage>(relaxed = true).apply {
            every { priority } returns 5
            every { notification } returns mockk {
                every { visibility } returns 3
                every { channelId } returns "channelId"
            }
        }
        // When
        notificationBuilder.setupNotification(data)
        // Then
        verify { notificationBuilder.setVisibility(3) }
        verify { notificationBuilder.setChannelId("channelId") }
    }

    @Test
    fun `setupNotification SHOULD not set visibility or channel WHEN notification is null`() {
        // Given
        val data = mockk<RemoteMessage>(relaxed = true).apply {
            every { priority } returns 5
            every { notification } returns null
        }
        // When
        notificationBuilder.setupNotification(data)
        // Then
        verify(exactly = 0) { notificationBuilder.setVisibility(any()) }
        verify(exactly = 0) { notificationBuilder.setChannelId(any()) }
    }

    @Test
    fun `setIntent SHOULD setContentIntent using DeeplinkHandler WHEN isCheckPush is true`() {
        // Given
        val data = mockk<AppcuesMessagingData>(relaxed = true)
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)
        mockkObject(DeepLinkHandler.Companion)
        every { DeepLinkHandler.getDebuggerValidationIntent(any(), any()) } returns mockk(relaxed = true)
        // When
        notificationBuilder.setIntent(mockk(relaxed = true), data, true)
        // Then
        verify { notificationBuilder.setContentIntent(any()) }
        unmockkAll()
    }

    @Test
    fun `setIntent SHOULD setContentIntent using PushDeeplinkHandler WHEN isCheckPush is false`() {
        // Given
        val data = mockk<AppcuesMessagingData>(relaxed = true)
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)
        mockkObject(PushDeeplinkHandler.Companion)
        every { PushDeeplinkHandler.getNotificationIntent(any(), any()) } returns mockk(relaxed = true)
        // When
        notificationBuilder.setIntent(mockk(relaxed = true), data, false)
        // Then
        verify { notificationBuilder.setContentIntent(any()) }
        unmockkAll()
    }

    @Test
    fun `notify SHOULD send notification using 1_100_100 WHEN isCheckPush is true`() {
        // Given
        val mockkNotificationManager = mockk<NotificationManagerCompat>(relaxed = true)
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns mockkNotificationManager
        // When
        notificationBuilder.notify(mockk(relaxed = true), true)
        // Then
        verify { mockkNotificationManager.notify(1_100_100, notificationBuilder.build()) }
        unmockkAll()
    }

    @Test
    fun `notify SHOULD send notification using incremental value starting from 1_000_000 WHEN isCheckPush is false`() {
        // Given
        val mockkNotificationManager = mockk<NotificationManagerCompat>(relaxed = true)
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns mockkNotificationManager
        // When
        notificationBuilder.notify(mockk(relaxed = true), false)
        notificationBuilder.notify(mockk(relaxed = true), false)
        // Then
        verify { mockkNotificationManager.notify(1_000_000, notificationBuilder.build()) }
        verify { mockkNotificationManager.notify(1_000_001, notificationBuilder.build()) }
        unmockkAll()
    }
}
