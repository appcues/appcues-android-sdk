package com.appcues

import android.app.Activity
import android.content.Intent
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.rules.KoinScopeRule
import com.appcues.rules.MainDispatcherRule
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class AppcuesTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var appcues: Appcues

    @Before
    fun setUp() {
        appcues = Appcues(get())
    }

    @Test
    fun `identify SHOULD NOT track WHEN userID is empty`() {
        // GIVEN
        val userId = ""
        val tracker: AnalyticsTracker = get()
        val sessionMonitor: SessionMonitor = get()

        // WHEN
        appcues.identify(userId)

        // THEN
        verify(exactly = 0) { tracker.identify(any()) }
        // called once at startup automatically, which is ignored, but not again since no valid user
        verify(exactly = 1) { sessionMonitor.start() }
    }

    @Test
    fun `identify SHOULD update Storage AND call AnalyticsTracker identify function AND start session`() {
        // GIVEN
        val userId = "default-0000"
        val properties = mapOf<String, Any>("prop" to "value")
        val storage: Storage = get()
        val sessionMonitor: SessionMonitor = get()
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.identify(userId, properties)

        // THEN
        assertThat(storage.userId).isEqualTo(userId)
        verify { tracker.identify(properties) }
        // called once at startup automatically, which is ignored, then again for the new valid user/session
        verify(exactly = 2) { sessionMonitor.start() }
    }

    @Test
    fun `track event SHOULD call AnalyticsTracker track function`() {
        // GIVEN
        val eventName = "test_event"
        val properties = mapOf<String, Any>("prop" to 1)
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.track(eventName, properties)

        // THEN
        verify { tracker.track(eventName, properties) }
    }

    @Test
    fun `track screen SHOULD call AnalyticsTracker screen function`() {
        // GIVEN
        val screenTitle = "test_screen"
        val properties = mapOf<String, Any>("prop" to true)
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.screen(screenTitle, properties)

        // THEN
        verify { tracker.screen(screenTitle, properties) }
    }

    @Test
    fun `group SHOULD update Storage and pass properties to AnalyticsTracker WHEN groupId is not null`() {
        // GIVEN
        val groupId = "test_group"
        val properties = mapOf<String, Any>("name" to true)
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.group(groupId, properties)

        // THEN
        assertThat(storage.groupId).isEqualTo(groupId)
        verify { tracker.group(properties) }
    }

    @Test
    fun `group SHOULD NOT pass properties to AnalyticsTracker WHEN groupId is null`() {
        // GIVEN
        val groupId: String? = null
        val properties = mapOf<String, Any>("name" to true)
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.group(groupId, properties)

        // THEN
        assertThat(storage.groupId).isEqualTo(groupId)
        verify { tracker.group(null) }
    }

    @Test
    fun `reset SHOULD call SessionMonitor reset AND update Storage`() {
        // GIVEN
        val storage: Storage = get()
        val sessionMonitor: SessionMonitor = get()

        // WHEN
        appcues.reset()

        // THEN
        assertThat(storage.userId).isEmpty()
        assertThat(storage.groupId).isNull()
        assertThat(storage.isAnonymous).isTrue()
        verify { sessionMonitor.reset() }
        verifyOrder {
            // important that the reset occurs before the user changes
            // so that analytics get attributed to the previous user first
            sessionMonitor.reset()
            storage.userId = ""
        }
    }

    @Test
    fun `anonymous SHOULD set Storage userId equal to the deviceId AND identify AND start a session`() {
        // GIVEN
        val properties = mapOf<String, Any>("prop" to 33)
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()
        val sessionMonitor: SessionMonitor = get()

        // WHEN
        appcues.anonymous(properties)

        // THEN
        assertThat(storage.userId).isEqualTo(storage.deviceId)
        assertThat(storage.isAnonymous).isTrue()
        // called once at startup automatically, which is ignored, then again for the new valid user/session
        verify(exactly = 2) { sessionMonitor.start() }
        verify { tracker.identify(properties) }
    }

    @Test
    fun `anonymous SHOULD use the custom user ID from configuration when set`() {
        // GIVEN
        val config: AppcuesConfig = get()
        val storage: Storage = get()
        val configUserId = "config_user_id"
        config.anonymousIdFactory = { configUserId }

        // WHEN
        appcues.anonymous()

        // THEN
        assertThat(storage.userId).isNotEqualTo(storage.deviceId)
        assertThat(storage.userId).isEqualTo(configUserId)
    }

    @Test
    fun `show SHOULD call ExperienceRenderer to show experience`() = runTest {
        // GIVEN
        val experienceId = UUID.randomUUID().toString()
        val experienceRenderer: ExperienceRenderer = get()

        // WHEN
        appcues.show(experienceId)

        // THEN
        coVerify { experienceRenderer.show(experienceId, ExperienceTrigger.ShowCall) }
    }

    @Test
    fun `debug SHOULD call AppcuesDebuggerManager to launch the debugger`() {
        // GIVEN
        val activity: Activity = mockk(relaxed = true)
        val debuggerManager: AppcuesDebuggerManager = get()

        // WHEN
        appcues.debug(activity)

        // THEN
        verify { debuggerManager.start(activity, Debugger(null)) }
    }

    @Test
    fun `stop SHOULD stop some background dependencies`() {
        // GIVEN
        val debuggerManager: AppcuesDebuggerManager = get()
        val activityScreenTracking: ActivityScreenTracking = get()
        val experienceRenderer: ExperienceRenderer = get()

        // WHEN
        appcues.stop()

        // THEN
        verify { debuggerManager.stop() }
        verify { activityScreenTracking.stop() }
        verify { experienceRenderer.stop() }
    }

    @Test
    fun `onNewIntent SHOULD call the DeepLinkHandler to handle the link`() {
        // GIVEN
        val activity: Activity = mockk(relaxed = true)
        val intent: Intent = mockk(relaxed = true)
        val deepLinkHandler: DeepLinkHandler = get()

        // WHEN
        appcues.onNewIntent(activity, intent)

        // THEN
        verify { deepLinkHandler.handle(activity, intent) }
    }

    @Test
    fun `trackScreens SHOULD call ActivityScreenTracking to enable screen tracking`() {
        // GIVEN
        val screenTracking: ActivityScreenTracking = get()

        // WHEN
        appcues.trackScreens()

        // THEN
        verify { screenTracking.trackScreens() }
    }

    @Test
    fun `registerTrait SHOULD call the TraitRegistry to register the trait`() {
        // GIVEN
        val registry: TraitRegistry = get()
        val type = "myTrait"
        val trait: ExperienceTrait = mockk()
        val factory: (Map<String, Any>?, ExperienceTraitLevel) -> ExperienceTrait = { _, _ -> trait }

        // WHEN
        appcues.registerTrait(type, factory)

        // THEN
        verify { registry.register(type, factory) }
    }

    @Test
    fun `registerAction SHOULD call the ActionRegistry to register the action`() {
        // GIVEN
        val registry: ActionRegistry = get()
        val type = "myAction"
        val action: ExperienceAction = mockk()
        val factory: (Map<String, Any>?) -> ExperienceAction = { action }

        // WHEN
        appcues.registerAction(type, factory)

        // THEN
        verify { registry.register(type, factory) }
    }

    @Test
    fun `group SHOULD pass null properties by default`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.group("x")

        // THEN
        verify { tracker.group(null) }
    }

    @Test
    fun `screen SHOULD pass null properties by default`() {
        // GIVEN
        val title = "screen"
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.screen(title)

        // THEN
        verify { tracker.screen(title, null) }
    }

    @Test
    fun `track SHOULD pass null properties by default`() {
        // GIVEN
        val name = "event"
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.track(name)

        // THEN
        verify { tracker.track(name, null) }
    }
}
