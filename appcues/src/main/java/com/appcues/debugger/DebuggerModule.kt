package com.appcues.debugger

import com.appcues.debugger.screencapture.GetCaptureUseCase
import com.appcues.debugger.screencapture.SaveCaptureUseCase
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL

internal object DebuggerModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped {
            DebuggerStatusManager(
                storage = get(),
                appcuesConfig = get(),
                appcuesRemoteSource = get(),
                contextWrapper = get(),
                analyticsTracker = get(),
            )
        }

        scoped {
            DebuggerRecentEventsManager(
                contextWrapper = get(),
                analyticsTracker = get(),
            )
        }

        scoped {
            DebuggerFontManager(appcuesConfig = get(), context = get(), logcues = get())
        }

        scoped { DebuggerLogMessageManager(get()) }

        scoped { SaveCaptureUseCase(sdkSettings = get(), customer = get(), imageUpload = get()) }
        scoped { GetCaptureUseCase() }
    }
}
