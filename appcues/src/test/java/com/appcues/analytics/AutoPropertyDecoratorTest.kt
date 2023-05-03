package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent.ScreenView
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.rules.MainDispatcherRule
import com.appcues.util.ContextResources
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AutoPropertyDecoratorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val config: AppcuesConfig = mockk<AppcuesConfig>(relaxed = true).apply {
        every { applicationId } returns "applicationId"
    }

    private val contextResources: ContextResources = mockk<ContextResources>(relaxed = true).apply {
        every { getAppVersion() } returns "1.0.0-A_TEST"
    }

    private val storage: Storage = mockk(relaxed = true)
    private val sessionMonitor: SessionMonitor = mockk(relaxed = true)
    private val sessionRandomizer: SessionRandomizer = mockk<SessionRandomizer>().apply {
        every { get() } returns 1
    }

    private lateinit var autoPropertyDecorator: AutoPropertyDecorator

    @Before
    fun setup() {
        autoPropertyDecorator = AutoPropertyDecorator(
            config = config,
            appcuesCoroutineScope = AppcuesCoroutineScope(mockk()),
            contextResources = contextResources,
            storage = storage,
            sessionMonitor = sessionMonitor,
            sessionRandomizer = sessionRandomizer,
        )
    }

    @Test
    fun `autoProperties SHOULD contain 21 amount of elements`() {
        assertThat(autoPropertyDecorator.autoProperties).hasSize(21)
    }

    @Test
    fun `autoProperties SHOULD contain 22 amount of elements WHEN one ScreenView is decorated`() {
        // given
        autoPropertyDecorator.decorateTrack(
            EventRequest(
                name = ScreenView.eventName,
                attributes = hashMapOf(
                    ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "Screen 1"
                )
            )
        )
        // then
        assertThat(autoPropertyDecorator.autoProperties).hasSize(22)
    }

    @Test
    fun `autoProperties SHOULD contain 23 amount of elements WHEN two or more ScreenView are decorated`() {
        // given
        autoPropertyDecorator.decorateTrack(
            EventRequest(
                name = ScreenView.eventName,
                attributes = hashMapOf(
                    ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "Screen 1"
                )
            )
        )
        autoPropertyDecorator.decorateTrack(
            EventRequest(
                name = ScreenView.eventName,
                attributes = hashMapOf(
                    ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "Screen 2"
                )
            )
        )
        // then
        assertThat(autoPropertyDecorator.autoProperties).hasSize(23)
    }

    @Test
    fun `decorateTrack SHOULD decorate context properties`() {
        // given
        val event = EventRequest(
            name = ScreenView.eventName,
        )
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            assertThat(context["app_id"]).isEqualTo("applicationId")
            assertThat(context["app_version"]).isEqualTo("1.0.0-A_TEST")
        }
    }

    @Test
    fun `decorateTrack SHOULD decorate identity properties WHEN event is ScreenView`() {
        // given
        val event = EventRequest(
            name = ScreenView.eventName,
            attributes = hashMapOf(
                ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "Screen 1"
            )
        )
        val event2 = EventRequest(
            name = ScreenView.eventName,
            attributes = hashMapOf(
                ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE to "Screen 2"
            )
        )
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            assertThat(attributes.containsKey("_identity")).isTrue()
            with(attributes["_identity"] as Map<*, *>) {
                assertThat(containsKey("_lastScreenTitle")).isFalse()
                assertThat(get("_currentScreenTitle")).isEqualTo("Screen 1")
                assertThat(get("_sessionPageviews")).isEqualTo(1)
            }
        }

        with(autoPropertyDecorator.decorateTrack(event2)) {
            // then
            assertThat(attributes.containsKey("_identity")).isTrue()
            with(attributes["_identity"] as Map<*, *>) {
                assertThat(get("_lastScreenTitle")).isEqualTo("Screen 1")
                assertThat(get("_currentScreenTitle")).isEqualTo("Screen 2")
                assertThat(get("_sessionPageviews")).isEqualTo(2)
            }
        }
    }

    @Test
    fun `decorateTrack SHOULD decorate identity properties WHEN event is SessionStarted`() {
        // given
        val event = EventRequest(
            name = SessionStarted.eventName,
        )
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            with(attributes["_identity"] as Map<*, *>) {
                assertThat(containsKey("_lastScreenTitle")).isFalse()
                assertThat(containsKey("_currentScreenTitle")).isFalse()
                assertThat(get("_sessionPageviews")).isEqualTo(0)
            }
        }
    }

    @Test
    fun `decorateTrack SHOULD decorate identity WITH new sessionRandomizer WHEN event is SessionStarted`() {
        // given
        val event = EventRequest(
            name = SessionStarted.eventName,
        )
        every { sessionRandomizer.get() } returns 10
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            with(attributes["_identity"] as Map<*, *>) {
                assertThat(get("_sessionRandomizer") as Int).isEqualTo(10)
            }
        }
        // given
        every { sessionRandomizer.get() } returns 82
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            with(attributes["_identity"] as Map<*, *>) {
                assertThat(get("_sessionRandomizer") as Int).isEqualTo(82)
            }
        }
    }

    @Test
    fun `decorateIdentity SHOULD add autoProperties to profileUpdate map`() {
        // given
        val activityRequest = ActivityRequest(userId = "test userId", accountId = "test accountId")
        // when
        with(autoPropertyDecorator.decorateIdentify(activityRequest)) {
            // then
            assertThat(profileUpdate).hasSize(21)
        }
    }

    @Test
    fun `decorateIdentity SHOULD put custom properties into profileUpdate map`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            accountId = "test accountId",
            profileUpdate = hashMapOf("_test" to "Test")
        )
        // when
        with(autoPropertyDecorator.decorateIdentify(activityRequest)) {
            // then
            assertThat(profileUpdate).hasSize(22)
            assertThat(profileUpdate!!["_test"]).isEqualTo("Test")
        }
    }

    @Test
    fun `decorateTrack SHOULD include additional properties from config`() {
        // given
        val config: AppcuesConfig = mockk<AppcuesConfig>(relaxed = true).apply {
            every { applicationId } returns "applicationId"
            every { additionalAutoProperties } returns mapOf("_myProp" to 100, "_sdkName" to "test-sdk")
        }
        autoPropertyDecorator = AutoPropertyDecorator(
            config = config,
            appcuesCoroutineScope = AppcuesCoroutineScope(mockk()),
            contextResources = contextResources,
            storage = storage,
            sessionMonitor = sessionMonitor,
            sessionRandomizer = sessionRandomizer,
        )
        val event = EventRequest(
            name = SessionStarted.eventName,
        )

        // when
        val decoratedEvent = autoPropertyDecorator.decorateTrack(event)

        // then
        with(decoratedEvent.attributes["_identity"] as Map<*, *>) {
            assertThat(get("_myProp") as Int).isEqualTo(100)
            assertThat(get("_sdkName") as String).isNotEqualTo("test-sdk")
        }
    }
}
