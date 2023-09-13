package com.appcues

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.appcues.data.AppcuesRepository
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.StateMachineDirectory
import com.appcues.util.ContextResources
import com.appcues.util.LinkOpener
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped {
            val config: AppcuesConfig = get()
            Logcues(config.loggingLevel)
        }
        scoped { Storage(context = get(), config = get()) }
        scoped {
            DeepLinkHandler(
                config = get(),
                experienceRenderer = get(),
                appcuesCoroutineScope = get(),
                debuggerManager = get(),
            )
        }
        scoped { AppcuesDebuggerManager(context = get(), koinScope = this) }
        scoped { StateMachineDirectory() }
        scoped { ContextResources(context = get()) }
        scoped { ExperienceRenderer(scope = get()) }
        scoped {
            AppcuesRepository(
                appcuesRemoteSource = get(),
                appcuesLocalSource = get(),
                experienceMapper = get(),
                config = get(),
                logcues = get(),
                storage = get(),
            )
        }
        scoped { LinkOpener(get()) }
        scoped { AnalyticsPublisher(storage = get()) }

        factory { StateMachine(appcuesCoroutineScope = get(), config = get(), actionProcessor = get(), lifecycleTracker = get()) }

        scoped {
            get<AppcuesConfig>().imageLoader ?: ImageLoader.Builder(context = get())
                .components {
                    if (VERSION.SDK_INT >= VERSION_CODES.P) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                    add(SvgDecoder.Factory())
                }.build()
        }
    }
}
