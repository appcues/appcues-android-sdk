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
            DebuggerFontManager(context = get(), logcues = get(), config = get())
        }

        scoped {
            ScreenCaptureProcessor(
                config = get(),
                contextResources = get(),
                sdkSettingsRemoteSource = get(),
                customerApiRemoteSource = get(),
                imageUploadRemoteSource = get(),
            )
        }
    }
}
