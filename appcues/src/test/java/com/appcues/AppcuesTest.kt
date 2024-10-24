package com.appcues

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.di.component.get
import com.appcues.di.scope.get
import com.appcues.rules.MainDispatcherRule
import com.appcues.rules.TestScopeRule
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel
import com.appcues.trait.TraitRegistry
import com.appcues.ui.AppcuesCustomComponentDirectory
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AppcuesTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var appcues: Appcues

    @Before
    fun setUp() {
        appcues = Appcues(get())
    }

    @Test
    fun `Appcues version SHOULD match BuildConfig`() {
        assertThat(appcues.version).isEqualTo(BuildConfig.SDK_VERSION)
    }

    @Test
    fun `Appcues call returns instance WITH same context, accountId and appId`() {
        // GIVEN
        val appContext = mockk<Application>()
        val context = mockk<Context>(relaxed = true) {
            every { applicationContext } returns appContext
        }
        // WHEN
        val appcues = Appcues(
            context = context,
            accountId = "1234-account",
            applicationId = "1234-appId"
        )
        // THEN
        with(appcues.scope.get<AppcuesConfig>()) {
            assertThat(accountId).isEqualTo("1234-account")
            assertThat(applicationId).isEqualTo("1234-appId")
        }

        assertThat(appcues.scope.get<Context>()).isEqualTo(appContext)
    }

    @Test
    fun `identify SHOULD NOT track WHEN userID is empty`() {
        // GIVEN
        val userId = ""
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.identify(userId)

        // THEN
        verify(exactly = 0) { tracker.identify(any()) }
    }

    @Test
    fun `identify SHOULD update Storage AND call AnalyticsTracker identify`() {
        // GIVEN
        val userId = "default-0000"
        val properties = mapOf<String, Any>("prop" to "value")
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.identify(userId, properties)

        // THEN
        assertThat(storage.userId).isEqualTo(userId)
        verify { tracker.identify(properties) }
    }

    @Test
    fun `identify SHOULD update Storage WHEN user signature provided`() {
        // GIVEN
        val userId = "default-0000"
        val properties = mapOf<String, Any>("appcues:user_id_signature" to "user-signature")
        val storage: Storage = get()

        // WHEN
        appcues.identify(userId, properties)

        // THEN
        assertThat(storage.userSignature).isEqualTo("user-signature")
    }

    @Test
    fun `identify SHOULD set user signature to null in Storage WHEN no user signature provided`() {
        // GIVEN
        val userId = "default-0000"
        val properties = mapOf<String, Any>("foo" to 123)
        val storage: Storage = get()

        // WHEN
        appcues.identify(userId, properties)

        // THEN
        assertThat(storage.userSignature).isNull()
    }

    @Test
    fun `identify SHOULD track device update WHEN same as existing user`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val storage: Storage = get()
        storage.userId = "test-user"

        // WHEN
        appcues.identify("test-user")

        // THEN
        verify { tracker.track(name = "appcues:device_updated", properties = null, interactive = true, isInternal = true) }
    }

    @Test
    fun `identify SHOULD NOT track device update WHEN new existing user`() {
        // no device update here since the new user will trigger a new session_started
        // event with device props
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val storage: Storage = get()
        storage.userId = "test-user"

        // WHEN
        appcues.identify("different-user")

        // THEN
        verify(exactly = 0) { tracker.track(name = "appcues:device_updated", properties = null, interactive = true, isInternal = true) }
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
    fun `reset SHOULD clear user signature`() {
        // GIVEN
        val userId = "default-0000"
        val properties = mapOf<String, Any>("appcues:user_id_signature" to "user-signature")
        val storage: Storage = get()

        // WHEN
        appcues.identify(userId, properties)
        appcues.reset()

        // THEN
        verify { storage setProperty Storage::userSignature.name value null }
    }

    @Test
    fun `anonymous SHOULD set Storage userId equal to the deviceId AND identify`() {
        // GIVEN
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()

        // WHEN
        appcues.anonymous()

        // THEN
        assertThat(storage.userId).isEqualTo("anon:${storage.deviceId}")
        assertThat(storage.isAnonymous).isTrue()
        verify { tracker.identify(null) }
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
        assertThat(storage.userId).isEqualTo("anon:$configUserId")
    }

    @Test
    fun `show SHOULD call ExperienceRenderer to show experience`() = runTest {
        // GIVEN
        val experienceId = UUID.randomUUID().toString()
        val experienceRenderer: ExperienceRenderer = get()

        // WHEN
        appcues.show(experienceId)

        // THEN
        coVerify { experienceRenderer.show(experienceId, ExperienceTrigger.ShowCall, mapOf()) }
    }

    @Test
    fun `debug SHOULD call AppcuesDebuggerManager to launch the debugger`() {
        // GIVEN
        val activity: Activity = mockk(relaxed = true)
        val debuggerManager: AppcuesDebuggerManager = get()

        // WHEN
        appcues.debug(activity)

        // THEN
        verify { debuggerManager.start(activity, Debugger) }
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
        coVerify { experienceRenderer.resetAll() }
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
    fun `registerEmbed SHOULD call the ExperienceRenderer start for given frame`() = runTest {
        // GIVEN
        val renderer: ExperienceRenderer = get()
        val view = mockk<AppcuesFrameView>(relaxed = true)

        // WHEN
        appcues.registerEmbed("frame1", view)

        // THEN
        coVerify { renderer.start(view, RenderContext.Embed("frame1")) }
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

    @Test
    fun `registerCustomComponent SHOULD add component to static dictionary`() {
        // Given
        val customComponent = mockk<AppcuesCustomComponentView>()
        // When
        Appcues.registerCustomComponent("1", customComponent)
        // Then
        assertThat(AppcuesCustomComponentDirectory.get("1")).isEqualTo(customComponent)
    }
}
