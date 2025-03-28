package com.appcues

import android.app.Application
import android.content.Context
import android.os.StrictMode
import coil.ImageLoader
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsModule
import com.appcues.analytics.AnalyticsQueueProcessor
import com.appcues.analytics.AnalyticsQueueProcessor.AnalyticsQueueScheduler
import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.AutoPropertyDecorator
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.analytics.SessionRandomizer
import com.appcues.data.AppcuesRepository
import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.DataLocalModule
import com.appcues.data.local.room.RoomAppcuesLocalSource
import com.appcues.data.mapper.DataMapperModule
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.DataRemoteModule
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.data.remote.customerapi.CustomerApiRemoteSource
import com.appcues.data.remote.imageupload.ImageUploadRemoteSource
import com.appcues.data.remote.sdksettings.SdkSettingsRemoteSource
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.debugger.DebuggerFontManager
import com.appcues.debugger.DebuggerModule
import com.appcues.debugger.DebuggerRecentEventsManager
import com.appcues.debugger.DebuggerStatusManager
import com.appcues.debugger.screencapture.GetCaptureUseCase
import com.appcues.debugger.screencapture.SaveCaptureUseCase
import com.appcues.di.Bootstrap
import com.appcues.di.definition.DefinitionParams
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.get
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.utils.ImageLoaderWrapper
import com.appcues.util.AppcuesViewTreeOwner
import com.appcues.util.ContextWrapper
import com.appcues.util.LinkOpener
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ModulesTests {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val appcuesConfig = AppcuesConfig("account", "app")

    private val context = mockk<Context>(relaxed = true) {
        every { applicationContext } returns mockk<Application>(relaxed = true)
    }

    private val imageLoader = mockk<ImageLoader>()
    private val imageLoaderWrapper = mockk<ImageLoaderWrapper> {
        every { build() } returns imageLoader
    }

    private val onExperienceEnded: (Experience) -> Unit = mockk(relaxed = true)

    private fun withScope(config: AppcuesConfig = appcuesConfig, scopedTest: AppcuesScope.() -> Unit) {
        val modules = arrayListOf(
            MainModule,
            AnalyticsModule,
            DataRemoteModule,
            DataMapperModule,
            DataLocalModule,
            DebuggerModule
        )

        val scope = Bootstrap.start(modules) {
            scoped { config }
            scoped<ContextWrapper> { mockk(relaxed = true) }
            scoped { imageLoaderWrapper }
            scoped { context.applicationContext }
        }
        with(scope) { scopedTest() }
    }

    @Before
    fun setup() {
        mockkStatic(StrictMode::class)
        every { StrictMode.allowThreadDiskReads() } returns mockk(relaxed = true)
        every { StrictMode.setThreadPolicy(any()) } returns Unit
    }

    @Test
    fun `check MainModule instances`() = withScope {
        get<Appcues>()
        get<AppcuesViewTreeOwner>()
        get<TraitRegistry>()
        get<ActionRegistry>()
        get<ActionProcessor>()
        get<CoroutineScope>()
        get<Logcues>()
        get<Storage>()
        get<DeepLinkHandler>()
        get<AppcuesDebuggerManager>()
        get<StateMachineDirectory>()
        get<ExperienceRenderer>()
        get<AppcuesRepository>()
        get<LinkOpener>()
        get<LinkOpener>()
        get<AnalyticsPublisher>()
        get<StateMachine>(DefinitionParams(listOf(onExperienceEnded)))
        assertThat(get<ImageLoader>()).isEqualTo(imageLoader)
    }

    @Test
    fun `check AnalyticsModule instances`() = withScope {
        get<SessionMonitor>()
        get<SessionRandomizer>()
        get<AutoPropertyDecorator>()
        get<ActivityRequestBuilder>()
        get<ExperienceLifecycleTracker>()
        get<ActivityScreenTracking>()
        assertThat(get<QueueScheduler>()).isInstanceOf(AnalyticsQueueScheduler::class.java)
        get<AnalyticsQueueProcessor>()
        get<AnalyticsTracker>()
    }

    @Test
    fun `check DataRemoteModule instance`() = withScope {
        get<AppcuesRemoteSource>()
        get<SdkSettingsRemoteSource>()
        get<CustomerApiRemoteSource>()
        get<ImageUploadRemoteSource>()
    }

    @Test
    fun `check DataMapperModule instance`() = withScope {
        get<ExperienceMapper>()
        get<StepMapper>()
        get<ActionsMapper>()
        get<TraitsMapper>()
    }

    @Test
    fun `check DataLocalModule instance`() = withScope {

        assertThat(get<AppcuesLocalSource>()).isInstanceOf(RoomAppcuesLocalSource::class.java)
    }

    @Test
    fun `check DebuggerModule instance`() = withScope {
        get<DebuggerStatusManager>()
        get<DebuggerRecentEventsManager>()
        get<DebuggerFontManager>()
        get<SaveCaptureUseCase>()
        get<GetCaptureUseCase>()
    }
}
