package com.appcues.push

import android.content.ActivityNotFoundException
import android.net.Uri
import com.appcues.Appcues
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.inject
import com.appcues.logging.Logcues
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.LinkOpener

internal class PushOpenedProcessor(
    override val scope: AppcuesScope,
) : AppcuesComponent {

    private val analyticsTracker by inject<AnalyticsTracker>()
    private val experienceRenderer by scope.inject<ExperienceRenderer>()
    private val linkOpener by scope.inject<LinkOpener>()
    private val appcues by scope.inject<Appcues>()
    private val logcues by scope.inject<Logcues>()

    private var deferredAction: PushOpenedAction? = null

    fun defer(action: PushOpenedAction) {
        deferredAction = action
    }

    suspend fun processDeferred(userId: String) {
        deferredAction?.let {
            if (it.userId == userId) process(it)
        }

        deferredAction = null
    }

    suspend fun process(pushAction: PushOpenedAction) {
        if (pushAction.isTest.not()) {
            analyticsTracker.track(pushAction.eventName, pushAction.eventProperties, interactive = false, isInternal = true)
        }

        pushAction.deeplink?.let {
            val uri = Uri.parse(it)
            try {
                // this will handle any in-app deep link scheme URLs OR any web urls that were
                // requested to open into the external browser application
                appcues.navigationHandler?.navigateTo(uri) ?: linkOpener.startNewIntent(uri)
            } catch (exception: ActivityNotFoundException) {
                logcues.error("Unable to process deep link $it\n\n Reason: ${exception.message}")
            }
        }

        pushAction.experienceId?.let {
            experienceRenderer.show(it, DeepLink, mapOf())
        }
    }
}
