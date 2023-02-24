package com.appcues.debugger

import com.appcues.debugger.screencapture.ScreenCaptureProcessor
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DebuggerKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped {
            DebuggerStatusManager(
                storage = get(),
                appcuesConfig = get(),
                appcuesRemoteSource = get(),
                contextResources = get(),
                context = get(),
            )
        }

        scoped {
            DebuggerRecentEventsManager(
                contextResources = get()
            )
        }

        scoped {
            DebuggerFontManager(context = get(), logcues = get())
        }

        scoped {
            ScreenCaptureProcessor(config = get(), contextResources = get())
        }
    }
}
