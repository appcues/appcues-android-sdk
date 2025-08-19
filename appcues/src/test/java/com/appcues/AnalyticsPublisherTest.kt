package com.appcues

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.AnalyticsEvent.ScreenView
import com.appcues.analytics.TrackingData
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.di.component.get
import com.appcues.rules.MainDispatcherRule
import com.appcues.rules.TestScopeRule
import com.appcues.util.DataSanitizer
import com.google.common.truth.Truth.assertThat
import io.mockk.called
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AnalyticsPublisherTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var analyticsPublisher: AnalyticsPublisher

    private data class TrackedEventTest(
        val type: AnalyticType,
        val value: String?,
        val properties: Map<String, Any>?,
        val isInternal: Boolean,
    )

    private val eventList: ArrayList<TrackedEventTest> = arrayListOf()

    private val testListener: AnalyticsListener = object : AnalyticsListener {
        override fun trackedAnalytic(type: AnalyticType, value: String?, properties: Map<String, Any>?, isInternal: Boolean) {
            eventList.add(TrackedEventTest(type, value, properties, isInternal))
        }
    }

    @Before
    fun setUp() {
        analyticsPublisher = AnalyticsPublisher(get(), get())
    }

    @Test
    fun `publish SHOULD do nothing when listener is null`() {
        // Given
        val trackingData = mockk<TrackingData>()
        // When
        analyticsPublisher.publish(null, trackingData)
        // Then
        verify { trackingData wasNot called }
    }

    @Test
    fun `publish SHOULD not call trackedAnalytic WHEN data type is event AND event list is empty`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf()
        )
        val data = TrackingData(EVENT, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(0)
    }

    @Test
    fun `publish SHOULD call trackedAnalytic of type EVENT once WHEN data type is event AND event list is one`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest("event1"))
        )
        val data = TrackingData(EVENT, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(1)
        assertThat(eventList[0].type).isEqualTo(EVENT)
        assertThat(eventList[0].value).isEqualTo("event1")
        assertThat(eventList[0].properties).isEmpty()
        assertThat(eventList[0].isInternal).isFalse()
    }

    @Test
    fun `publish SHOULD call trackedAnalytic of type EVENT twice WHEN data type is event AND event list is two`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest("event1"), EventRequest("event2"))
        )
        val data = TrackingData(EVENT, true, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(2)
        assertThat(eventList[0].type).isEqualTo(EVENT)
        assertThat(eventList[0].value).isEqualTo("event1")
        assertThat(eventList[0].isInternal).isTrue()
        assertThat(eventList[1].type).isEqualTo(EVENT)
        assertThat(eventList[1].value).isEqualTo("event2")
        assertThat(eventList[1].isInternal).isTrue()
    }

    @Test
    fun `publish SHOULD sanitize attributes for for type EVENT`() {
        // Given
        val attributes = mutableMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest("event1", attributes = attributes))
        )
        val data = TrackingData(EVENT, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        verify(exactly = 1) { with(get<DataSanitizer>()) { attributes.sanitize() } }
    }

    @Test
    @Ignore("Currently getting userId from storage.userId which depends on external factors to be right.")
    fun `publish SHOULD call trackedAnalytic of type IDENTIFY WHEN data type is identify`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
        )
        val data = TrackingData(IDENTIFY, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(1)
        assertThat(eventList[0].type).isEqualTo(IDENTIFY)
        assertThat(eventList[0].value).isEqualTo("userId")
        assertThat(eventList[0].properties).isNull()
        assertThat(eventList[0].isInternal).isFalse()
        // Validate sanitize was not called when profileUpdate is null
        verify { with(get<DataSanitizer>()) { any<Map<*, *>>().sanitize() } wasNot called }
    }

    @Test
    fun `publish SHOULD sanitize profileUpdate for for type IDENTIFY WHEN profileUpdate is not null`() {
        // Given
        val profileUpdate = mutableMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            profileUpdate = profileUpdate,
            sessionId = UUID.randomUUID(),
        )
        val data = TrackingData(IDENTIFY, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        verify(exactly = 1) { with(get<DataSanitizer>()) { profileUpdate.sanitize() } }
    }

    @Test
    @Ignore("Currently getting groupId from storage.group which depends on external factors to be right.")
    fun `publish SHOULD call trackedAnalytic of type GROUP WHEN data type is group`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            groupId = "groupId",
            sessionId = UUID.randomUUID(),
        )
        val data = TrackingData(GROUP, true, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(1)
        assertThat(eventList[0].type).isEqualTo(GROUP)
        assertThat(eventList[0].value).isEqualTo("groupId")
        assertThat(eventList[0].properties).isNull()
        assertThat(eventList[0].isInternal).isTrue()
        // Validate sanitize was not called when groupUpdate is null
        verify { with(get<DataSanitizer>()) { any<Map<*, *>>().sanitize() } wasNot called }
    }

    @Test
    fun `publish SHOULD sanitize groupUpdate for for type GROUP WHEN groupUpdate is not null`() {
        // Given
        val groupUpdate = mutableMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            groupUpdate = groupUpdate,
            sessionId = UUID.randomUUID(),
        )
        val data = TrackingData(GROUP, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        verify(exactly = 1) { with(get<DataSanitizer>()) { groupUpdate.sanitize() } }
    }

    @Test
    fun `publish SHOULD not call trackedAnalytic of type SCREEN WHEN data type is screen AND event list is empty`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf()
        )
        val data = TrackingData(SCREEN, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(0)
    }

    @Test
    fun `publish SHOULD call trackedAnalytic of type SCREEN once WHEN data type is screen AND event list is one`() {
        // Given
        val attributes = hashMapOf<String, Any>(ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "home")
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest(ScreenView.eventName, attributes = attributes))
        )
        val data = TrackingData(SCREEN, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(1)
        assertThat(eventList[0].type).isEqualTo(SCREEN)
        assertThat(eventList[0].value).isEqualTo("home")
        assertThat(eventList[0].properties).isEmpty()
        assertThat(eventList[0].isInternal).isFalse()
    }

    @Test
    fun `publish SHOULD call trackedAnalytic of type SCREEN twice WHEN data type is screen AND event list is two`() {
        // Given
        val attributes = hashMapOf<String, Any>(ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "home")
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest(ScreenView.eventName, attributes = attributes), EventRequest("event2"))
        )
        val data = TrackingData(SCREEN, true, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(2)
        assertThat(eventList[0].type).isEqualTo(SCREEN)
        assertThat(eventList[0].value).isEqualTo("home")
        assertThat(eventList[0].isInternal).isTrue()
        /**
         * Should not be allowed since we are reporting it as a SCREEN event when its not a "appcues:screen_view" event
         *
         * @see AnalyticsPublisher
         */
        assertThat(eventList[1].type).isEqualTo(SCREEN)
        assertThat(eventList[1].value).isEqualTo(null)
        assertThat(eventList[1].isInternal).isTrue()
    }

    @Test
    fun `publish SHOULD sanitize attributes for for type SCREEN`() {
        // Given
        val attributes = hashMapOf<String, Any>(ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "home", "prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest(ScreenView.eventName, attributes = attributes))
        )
        val data = TrackingData(SCREEN, false, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        verify(exactly = 1) { with(get<DataSanitizer>()) { attributes.sanitize() } }
    }

    @Test
    @Ignore("We should filter events that are appcues:screen_view when publishing for type SCREEN")
    fun `publish SHOULD should not call trackedAnalytic of type SCREEN WHEN EventRequest is not appcues screen_view`() {
        // Given
        val activity = ActivityRequest(
            accountId = "123",
            appId = "appId",
            userId = "userId",
            sessionId = UUID.randomUUID(),
            events = listOf(EventRequest("event2"))
        )
        val data = TrackingData(SCREEN, true, activity)
        // When
        analyticsPublisher.publish(testListener, data)
        // Then
        assertThat(eventList).hasSize(0)
    }
}
