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
import com.appcues.rules.KoinScopeRule
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class AnalyticsPublisherTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

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
        analyticsPublisher = AnalyticsPublisher(get())
    }

    @Test
    fun `analyticsPublisher SHOULD sanitize Date objects to Double in properties map`() {
        // GIVEN
        val dateLong = 1666102372942
        val attributes = hashMapOf(
            "date" to Date(dateLong),
            "map" to hashMapOf<String, Any>(
                "date" to Date(dateLong)
            ),
            "list" to listOf<Any>(Date(dateLong))
        )
        val activity = ActivityRequest(
            accountId = "123",
            userId = "userId",
            events = listOf(EventRequest("event1", attributes = attributes))
        )
        val data = TrackingData(EVENT, false, activity)

        // WHEN
        analyticsPublisher.publish(testListener, data)

        // THEN
        assertThat(eventList).hasSize(1)

        with(eventList[0]) {
            assertThat(properties).hasSize(3)
            assertThat(properties).containsEntry("date", dateLong.toDouble())
            assertThat(properties!!["map"] as Map<*, *>).containsEntry("date", dateLong.toDouble())
            assertThat(properties["list"] as List<*>).containsExactly(dateLong.toDouble())
        }
    }

    @Test
    fun `analyticsListener SHOULD track event WHEN event TrackingData is published`() {
        // GIVEN
        val attributes = hashMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            userId = "userId",
            events = listOf(EventRequest("event1", attributes = attributes))
        )
        val data = TrackingData(EVENT, false, activity)
        val listener = mockk<AnalyticsListener>(relaxed = true)

        // WHEN
        analyticsPublisher.publish(listener, data)

        // THEN
        verify { listener.trackedAnalytic(EVENT, "event1", attributes, false) }
    }

    @Test
    fun `analyticsListener SHOULD track screen WHEN screen TrackingData is published`() {
        // GIVEN
        val attributes = hashMapOf<String, Any>(ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "screen1")
        val activity = ActivityRequest(
            accountId = "123",
            userId = "userId",
            events = listOf(EventRequest(ScreenView.eventName, attributes = attributes))
        )
        val data = TrackingData(SCREEN, false, activity)
        val listener = mockk<AnalyticsListener>(relaxed = true)

        // WHEN
        analyticsPublisher.publish(listener, data)

        // THEN
        verify { listener.trackedAnalytic(SCREEN, "screen1", attributes, false) }
    }

    @Test
    fun `analyticsListener SHOULD track identify WHEN identify TrackingData is published`() {
        // GIVEN
        val storage: Storage = get()
        storage.userId = "userId"
        val attributes = hashMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            userId = storage.userId,
            profileUpdate = attributes
        )
        val data = TrackingData(IDENTIFY, false, activity)
        val listener = mockk<AnalyticsListener>(relaxed = true)

        // WHEN
        analyticsPublisher.publish(listener, data)

        // THEN
        verify { listener.trackedAnalytic(IDENTIFY, "userId", attributes, false) }
    }

    @Test
    fun `analyticsListener SHOULD track group WHEN group TrackingData is published`() {
        // GIVEN
        val storage: Storage = get()
        storage.groupId = "groupId"
        val attributes = hashMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            userId = "userId",
            groupId = storage.groupId,
            groupUpdate = attributes
        )
        val data = TrackingData(GROUP, false, activity)
        val listener = mockk<AnalyticsListener>(relaxed = true)

        // WHEN
        analyticsPublisher.publish(listener, data)

        // THEN
        verify { listener.trackedAnalytic(GROUP, "groupId", attributes, false) }
    }

    @Test
    fun `analyticsListener SHOULD track internal event WHEN event TrackingData is published AND isInternal equals true`() {
        // GIVEN
        val attributes = hashMapOf<String, Any>("prop" to 42)
        val activity = ActivityRequest(
            accountId = "123",
            userId = "userId",
            events = listOf(EventRequest("event1", attributes = attributes))
        )
        val data = TrackingData(EVENT, true, activity)
        val listener = mockk<AnalyticsListener>(relaxed = true)

        // WHEN
        analyticsPublisher.publish(listener, data)

        // THEN
        verify { listener.trackedAnalytic(EVENT, "event1", attributes, true) }
    }
}
