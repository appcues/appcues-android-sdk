package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent.DeviceUpdated
import com.appcues.analytics.AnalyticsEvent.ExperienceStepSeen
import com.appcues.analytics.AnalyticsEvent.ScreenView
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.rules.MainDispatcherRule
import com.appcues.util.ContextWrapper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AutoPropertyDecoratorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val config: AppcuesConfig = mockk<AppcuesConfig>(relaxed = true).apply {
        every { applicationId } returns "applicationId"
    }

    private val contextWrapper: ContextWrapper = mockk<ContextWrapper>(relaxed = true).apply {
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
            contextWrapper = contextWrapper,
            storage = storage,
            sessionMonitor = sessionMonitor,
            sessionRandomizer = sessionRandomizer,
        )
    }

    @Test
    fun `autoProperties SHOULD contain proper amount of elements`() {
        assertThat(autoPropertyDecorator.autoProperties).hasSize(22)
        with(autoPropertyDecorator.autoProperties) {
            // App
            assertThat(containsKey("_appId")).isTrue()
            assertThat(containsKey("_operatingSystem")).isTrue()
            assertThat(containsKey("_bundlePackageId")).isTrue()
            assertThat(containsKey("_appName")).isTrue()
            assertThat(containsKey("_appVersion")).isTrue()
            assertThat(containsKey("_appBuild")).isTrue()
            assertThat(containsKey("_sdkVersion")).isTrue()
            assertThat(containsKey("_sdkName")).isTrue()
            assertThat(containsKey("_osVersion")).isTrue()
            assertThat(containsKey("_deviceType")).isTrue()
            assertThat(containsKey("_deviceModel")).isTrue()
            // session
            assertThat(containsKey("userId")).isTrue()
            assertThat(containsKey("_isAnonymous")).isTrue()
            assertThat(containsKey("_localId")).isTrue()
            assertThat(containsKey("_updatedAt")).isTrue()
            assertThat(containsKey("_lastSeenAt")).isTrue()
            assertThat(containsKey("_sessionId")).isTrue()
            assertThat(containsKey("_lastContentShownAt")).isTrue()
            assertThat(containsKey("_lastBrowserLanguage")).isTrue()
            assertThat(containsKey("_sessionPageviews")).isTrue()
            assertThat(containsKey("_sessionRandomizer")).isTrue()
            assertThat(containsKey("_pushPrimerEligible")).isTrue()
        }
    }

    @Test
    fun `autoProperties SHOULD contain proper amount of elements WHEN one ScreenView is decorated`() {
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
        assertThat(autoPropertyDecorator.autoProperties).hasSize(23)
    }

    @Test
    fun `autoProperties SHOULD contain correct amount of elements WHEN two or more ScreenView are decorated`() {
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
        assertThat(autoPropertyDecorator.autoProperties).hasSize(24)
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
    fun `decorateTrack SHOULD decorate _device properties WHEN event is SessionStarted`() {
        // given
        val event = EventRequest(name = SessionStarted.eventName)
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            assertThat(attributes.containsKey("_device")).isTrue()

            with(attributes["_device"] as Map<*, *>) {
                assertThat(containsKey("_appId")).isTrue()
                assertThat(containsKey("_operatingSystem")).isTrue()
                assertThat(containsKey("_bundlePackageId")).isTrue()
                assertThat(containsKey("_appName")).isTrue()
                assertThat(containsKey("_appVersion")).isTrue()
                assertThat(containsKey("_appBuild")).isTrue()
                assertThat(containsKey("_sdkVersion")).isTrue()
                assertThat(containsKey("_sdkName")).isTrue()
                assertThat(containsKey("_osVersion")).isTrue()
                assertThat(containsKey("_deviceType")).isTrue()
                assertThat(containsKey("_deviceModel")).isTrue()
                assertThat(containsKey("_deviceId")).isTrue()
                assertThat(containsKey("_language")).isTrue()
                assertThat(containsKey("_pushToken")).isTrue()
                assertThat(containsKey("_pushEnabledBackground")).isTrue()
                assertThat(containsKey("_pushEnabled")).isTrue()
            }
        }
    }

    @Test
    fun `decorateTrack SHOULD decorate _device properties WHEN event is DeviceUpdated`() {
        // given
        val event = EventRequest(name = DeviceUpdated.eventName)
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            assertThat(attributes.containsKey("_device")).isTrue()
        }
    }

    @Test
    fun `decorateTrack SHOULD NOT include _device properties WHEN event is not SessionStarted`() {
        // given
        val event = EventRequest(name = ExperienceStepSeen.eventName)
        // when
        with(autoPropertyDecorator.decorateTrack(event)) {
            // then
            assertThat(attributes.containsKey("_device")).isFalse()
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
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            sessionId = UUID.randomUUID(),
            accountId = "test accountId"
        )
        // when
        with(autoPropertyDecorator.decorateIdentify(activityRequest)) {
            // then
            assertThat(profileUpdate).hasSize(22)
        }
    }

    @Test
    fun `decorateIdentity SHOULD add session properties and send on other requests`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            accountId = "test accountId",
            sessionId = UUID.randomUUID(),
            profileUpdate = hashMapOf("test_property" to "test_value")
        )
        // when
        with(autoPropertyDecorator.decorateIdentify(activityRequest)) {
            // then
            assertThat(profileUpdate).hasSize(23)
        }
        // then when
        with(autoPropertyDecorator.decorateTrack(EventRequest(name = SessionStarted.eventName))) {
            assertThat(attributes["_identity"] as HashMap<*, *>).containsEntry("test_property", "test_value")
        }
    }

    @Test
    fun `decorateIdentity SHOULD put custom properties into profileUpdate map`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            accountId = "test accountId",
            sessionId = UUID.randomUUID(),
            profileUpdate = hashMapOf("_test" to "Test")
        )
        // when
        with(autoPropertyDecorator.decorateIdentify(activityRequest)) {
            // then
            assertThat(profileUpdate).hasSize(23)
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
            contextWrapper = contextWrapper,
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

    @Test
    fun `decorateGroup SHOULD add autoProperties to groupUpdate map WHEN group is not null`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            sessionId = UUID.randomUUID(),
            accountId = "test accountId",
            groupId = "group"
        )
        // when
        with(autoPropertyDecorator.decorateGroup(activityRequest)) {
            // then
            assertThat(groupUpdate).hasSize(1)
            assertThat(groupUpdate).containsKey("_lastSeenAt")
        }
    }

    @Test
    fun `decorateGroup SHOULD NOT add autoProperties to groupUpdate map WHEN group is null`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            sessionId = UUID.randomUUID(),
            accountId = "test accountId",
            groupId = null
        )
        // when
        with(autoPropertyDecorator.decorateGroup(activityRequest)) {
            // then
            assertThat(groupUpdate).isNull()
        }
    }

    @Test
    fun `decorateGroup SHOULD include custom properties into groupUpdate map`() {
        // given
        val activityRequest = ActivityRequest(
            userId = "test userId",
            appId = "appId",
            accountId = "test accountId",
            sessionId = UUID.randomUUID(),
            groupId = "group",
            groupUpdate = hashMapOf("prop" to 12)
        )
        // when
        with(autoPropertyDecorator.decorateGroup(activityRequest)) {
            // then
            assertThat(groupUpdate).hasSize(2)
            assertThat(groupUpdate!!["prop"]).isEqualTo(12)
            assertThat(groupUpdate).containsKey("_lastSeenAt")
        }
    }
}
