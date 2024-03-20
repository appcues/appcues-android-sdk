package com.appcues

import android.app.Activity
import android.content.Intent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.ExperienceRenderer.PreviewResponse.ExperienceNotFound
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Failed
import com.appcues.ui.ExperienceRenderer.PreviewResponse.PreviewDeferred
import com.appcues.ui.ExperienceRenderer.PreviewResponse.StateMachineError
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class DeeplinkHandlerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var deepLinkHandler: DeepLinkHandler

    private val config: AppcuesConfig = mockk(relaxed = true)
    private val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
    private val appcuesCoroutineScope: AppcuesCoroutineScope = AppcuesCoroutineScope(Logcues())
    private val debuggerManager: AppcuesDebuggerManager = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    @Before
    fun setUp() {
        deepLinkHandler = DeepLinkHandler(config, experienceRenderer, appcuesCoroutineScope, debuggerManager, analyticsTracker)
    }

    @Test
    fun `handle SHOULD return false WHEN intent data is null`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns null
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `handle SHOULD return false WHEN intent action is not ACTION_VIEW`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_CALL
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `handle SHOULD return true WHEN scheme is appcues-appId`() {
        // GIVEN
        every { config.applicationId } returns "5555-ABCD"
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-5555-ABCD"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "1234")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return true WHEN path is experience_preview-1234`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "1234")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return true WHEN path is experience_content-1234`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_content", "1234")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return true WHEN path is debugger`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("debugger")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return true WHEN path is debugger and sub-path`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("debugger", "debugger-path")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return true WHEN path is capture_screen WITH token`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("capture_screen")
                every { getQueryParameter("token") } returns "1234"
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `handle SHOULD return false WHEN path is capture_screen WITHOUT token`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("capture_screen")
                every { getQueryParameter("token") } returns null
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `handle SHOULD return false WHEN path is not valid`() {
        // GIVEN
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("invalid")
            }
        }

        // WHEN
        val result = deepLinkHandler.handle(mockk(relaxed = true), intent)

        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `handle SHOULD call debugger screen capture WITH proper information`() = runTest {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("capture_screen")
                every { getQueryParameter("token") } returns "token-1234"
            }
        }

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        coVerify { debuggerManager.start(activity, ScreenCapture("token-1234")) }
    }

    @Test
    fun `handle SHOULD call debugger pages WITH path null`() = runTest {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("debugger")
            }
        }

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        coVerify { debuggerManager.start(activity, Debugger) }
    }

    @Test
    fun `handle SHOULD call debugger pages WITH path deeplink-path`() = runTest {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("debugger", "deeplink-path")
            }
        }

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        coVerify { debuggerManager.start(activity, Debugger, "deeplink-path") }
    }

    @Test
    fun `handle SHOULD call show WITH experienceId and query`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_content", "experienceId-1234")
                every { queryParameterNames } returns setOf("param1", "param2")
                every { getQueryParameter("param1") } returns "value1"
                every { getQueryParameter("param2") } returns "value2"
            }
        }

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        coVerify { experienceRenderer.show("experienceId-1234", DeepLink, mapOf("param1" to "value1", "param2" to "value2")) }
    }

    @Test
    fun `handle SHOULD call preview WITH experienceId and query`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
                every { queryParameterNames } returns setOf("param1", "param2")
                every { getQueryParameter("param1") } returns "value1"
                every { getQueryParameter("param2") } returns "value2"
            }
        }

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        coVerify { experienceRenderer.preview("experienceId-1234", mapOf("param1" to "value1", "param2" to "value2")) }
    }

    @Test
    fun `WHEN preview returns Failed SHOULD call resources flow_failed`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns Failed

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources.getString(R.string.appcues_preview_flow_failed) }
    }

    @Test
    fun `WHEN preview returns PreviewDeferred WITH frameId null SHOULD call resources flow_failed`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns PreviewDeferred(mockk(), null)

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources.getString(R.string.appcues_preview_flow_failed) }
    }

    @Test
    fun `WHEN preview returns PreviewDeferred WITH frameId SHOULD call resources preview_embed_message`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val experience = mockk<Experience>(relaxed = true) {
            every { name } returns "experience-name"
        }
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns PreviewDeferred(experience, "frame1234")

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources.getString(R.string.appcues_preview_embed_message, "frame1234", "experience-name") }
    }

    @Test
    fun `WHEN preview returns StateMachineError SHOULD call resources preview_flow_failed_reason`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val experience = mockk<Experience>(relaxed = true) {
            every { name } returns "experience-name"
        }
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns StateMachineError(experience, ExperienceAlreadyActive)

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources.getString(R.string.appcues_preview_flow_failed_reason, "experience-name", "Experience already active") }
    }

    @Test
    fun `WHEN preview returns ExperienceNotFound SHOULD call resources preview_flow_not_found`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns ExperienceNotFound

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources.getString(R.string.appcues_preview_flow_not_found) }
    }

    @Test
    fun `WHEN preview returns Success SHOULD not call resources`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val intent = mockk<Intent> {
            every { action } returns Intent.ACTION_VIEW
            every { data } returns mockk(relaxed = true) {
                every { scheme } returns "appcues-democues"
                every { host } returns "sdk"
                every { pathSegments } returns listOf("experience_preview", "experienceId-1234")
            }
        }
        coEvery { experienceRenderer.preview("experienceId-1234", mapOf()) } returns Success

        // WHEN
        deepLinkHandler.handle(activity, intent)

        // THEN
        verify { activity.resources wasNot called }
    }
}
