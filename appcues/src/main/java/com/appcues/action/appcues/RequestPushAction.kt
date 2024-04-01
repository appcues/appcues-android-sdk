package com.appcues.action.appcues

import android.content.Context
import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.ui.RequestPermissionActivity
import kotlinx.coroutines.CompletableDeferred

internal class RequestPushAction(
    override val config: AppcuesConfigMap,
    private val context: Context,
    private val analyticsTracker: AnalyticsTracker,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/request-push"
    }

    override suspend fun execute() {
        val completion = CompletableDeferred<Boolean>()
        RequestPermissionActivity.completion = completion

        // so passing in the actual string (matching the manifest value) ensures that this string exist in all versions of android
        // since this key was limited by @RequiresApi(VERSION_CODES.TIRAMISU), in case its a invalid string nothing happens
        context.startActivity(RequestPermissionActivity.getIntent(context, "android.permission.POST_NOTIFICATIONS"))

        completion.await()

        analyticsTracker.track(AnalyticsEvent.DeviceUpdated.eventName, properties = null, interactive = false, isInternal = true)
    }
}
