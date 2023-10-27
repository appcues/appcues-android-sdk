package com.appcues.debugger

import com.appcues.debugger.screencapture.ScreenCaptureProcessor
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
                context = get(),
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
            DebuggerFontManager(context = get(), logcues = get())
        }

        scoped { DebuggerLogMessageManager(get()) }

        scoped {
            ScreenCaptureProcessor(
                config = get(),
                contextWrapper = get(),
                sdkSettingsRemoteSource = get(),
                customerApiRemoteSource = get(),
                imageUploadRemoteSource = get(),
            )
        }
    }
}
