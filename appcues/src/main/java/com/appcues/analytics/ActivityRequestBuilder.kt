package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest

internal class ActivityRequestBuilder(
    private val config: AppcuesConfig,
    private val storage: Storage,
    private val decorator: AutoPropertyDecorator,
) {

    companion object {
        const val SCREEN_TITLE_ATTRIBUTE = "screenTitle"
        const val SCREEN_TITLE_CONTEXT = "screen_title"
    }

    fun identify(properties: HashMap<String, Any>? = null) = decorator.decorateIdentify(
        ActivityRequest(
            userId = storage.userId,
            accountId = config.accountId,
            groupId = storage.groupId,
            profileUpdate = properties
        )
    )

    fun group(properties: HashMap<String, Any>? = null) = ActivityRequest(
        userId = storage.userId,
        accountId = config.accountId,
        groupId = storage.groupId,
        groupUpdate = properties // no auto-properties on group calls
    )

    fun track(name: String, properties: HashMap<String, Any>? = null) = ActivityRequest(
        userId = storage.userId,
        accountId = config.accountId,
        groupId = storage.groupId,
        events = listOf(decorator.decorateTrack(EventRequest(name = name, attributes = properties ?: hashMapOf())))
    )

    fun screen(title: String, properties: HashMap<String, Any>? = null) = ActivityRequest(
        userId = storage.userId,
        accountId = config.accountId,
        groupId = storage.groupId,
        events = listOf(
            decorator.decorateTrack(
                EventRequest(
                    // screen calls are really just a special type of event: "appcues:screen_view"
                    name = AnalyticsEvent.ScreenView.eventName,
                    attributes = (properties ?: hashMapOf()).apply { put(SCREEN_TITLE_ATTRIBUTE, title) },
                    context = hashMapOf(SCREEN_TITLE_CONTEXT to title)
                )
            )
        )
    )
}
