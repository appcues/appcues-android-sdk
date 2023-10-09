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
            )
        }

        scoped {
            DebuggerRecentEventsManager(
                contextWrapper = get()
            )
        }

        scoped {
            DebuggerFontManager(context = get(), logcues = get())
        }

        scoped {
            ScreenCaptureProcessor(
                config = get(),
                contextWrapper = get(),
                appcuesBundleRemoteSource = get(),
                customerApiRemoteSource = get(),
                imageUploadRemoteSource = get(),
            )
        }
    }
}
