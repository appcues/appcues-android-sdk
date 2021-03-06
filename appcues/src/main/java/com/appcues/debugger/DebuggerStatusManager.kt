package com.appcues.debugger

import android.os.Build
import android.os.Build.VERSION
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.StatusType.ERROR
import com.appcues.debugger.model.StatusType.EXPERIENCE
import com.appcues.debugger.model.StatusType.LOADING
import com.appcues.debugger.model.StatusType.PHONE
import com.appcues.debugger.model.StatusType.SUCCESS
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.model.TapActionType.HEALTH_CHECK
import com.appcues.util.ContextResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

internal class DebuggerStatusManager(
    storage: Storage,
    private val appcuesConfig: AppcuesConfig,
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val contextResources: ContextResources,
) {

    private var connectedToAppcues: Boolean? = false

    private var trackingScreens: Boolean? = null

    private var userIdentified: String? = storage.userId.ifEmpty { null }

    private var experienceName: String? = null

    private var experienceShowingStep: String? = null

    private val _data = MutableStateFlow<List<DebuggerStatusItem>>(arrayListOf())

    val data: StateFlow<List<DebuggerStatusItem>>
        get() = _data

    suspend fun start() = withContext(Dispatchers.IO) {
        connectToAppcues(false)
    }

    suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        userIdentified = activityRequest.userId.ifEmpty { null }

        activityRequest.events?.forEach { event ->
            when (event.name) {
                AnalyticsEvent.ScreenView.eventName -> {
                    trackingScreens = true
                }
                AnalyticsEvent.ExperienceStarted.eventName -> {
                    experienceName = event.attributes["experienceName"] as String
                }
                AnalyticsEvent.ExperienceStepSeen.eventName -> {
                    (event.attributes["stepIndex"] as String).split(",").also {
                        val group = it.first().toInt() + 1
                        val step = it.last().toInt() + 1

                        experienceShowingStep = contextResources.getString(R.string.appcues_debugger_status_experience_line1, group, step)
                    }
                }
                AnalyticsEvent.ExperienceCompleted.eventName, AnalyticsEvent.ExperienceDismissed.eventName -> {
                    experienceName = null
                    experienceShowingStep = null
                }
                AnalyticsEvent.SessionReset.eventName -> {
                    userIdentified = null
                }
                else -> Unit
            }
        }

        updateData()
    }

    private suspend fun updateData() {
        _data.emit(
            arrayListOf(
                deviceInfoItem(),
                sdkInfoItem(),
                connectionCheckItem(),
                trackingScreenCheckItem(),
                identityItem()
            ).appendExperienceIfAny()
        )
    }

    private fun deviceInfoItem() = DebuggerStatusItem(
        title = contextResources.getString(R.string.appcues_debugger_status_device_title, Build.MANUFACTURER, VERSION.RELEASE),
        statusType = PHONE,
    )

    private fun sdkInfoItem() = DebuggerStatusItem(
        title = contextResources.getString(R.string.appcues_debugger_status_sdk_title, BuildConfig.SDK_VERSION),
        statusType = SUCCESS,
        line1 = contextResources.getString(R.string.appcues_debugger_status_sdk_line1, appcuesConfig.accountId),
        line2 = contextResources.getString(R.string.appcues_debugger_status_sdk_line2, appcuesConfig.applicationId)
    )

    private fun connectionCheckItem() = (connectedToAppcues?.let { if (it) SUCCESS else ERROR } ?: LOADING).let { statusType ->
        DebuggerStatusItem(
            title = statusType.let {
                when (it) {
                    SUCCESS -> R.string.appcues_debugger_status_check_connection_connected_title
                    LOADING -> R.string.appcues_debugger_status_check_connection_connecting_title
                    else -> R.string.appcues_debugger_status_check_connection_error_title
                }
            }.let { contextResources.getString(it) },
            line1 = statusType.let {
                when (it) {
                    ERROR -> R.string.appcues_debugger_status_check_connection_error_line1
                    else -> null
                }
            }?.let { contextResources.getString(it) },
            statusType = statusType,
            showRefreshIcon = statusType != LOADING,
            tapActionType = HEALTH_CHECK
        )
    }

    private fun trackingScreenCheckItem() = (trackingScreens?.let { if (it) SUCCESS else ERROR } ?: LOADING).let { statusType ->
        DebuggerStatusItem(
            title = contextResources.getString(R.string.appcues_debugger_status_check_screen_tracking_title),
            line1 = statusType.let {
                when (it) {
                    LOADING -> R.string.appcues_debugger_status_check_screen_tracking_loading_line1
                    else -> null
                }
            }?.let { contextResources.getString(it) },
            statusType = statusType,
        )
    }

    private fun identityItem() = userIdentified?.let {
        DebuggerStatusItem(
            title = contextResources.getString(R.string.appcues_debugger_status_identity_success_title),
            line1 = userIdentified,
            statusType = SUCCESS,
        )
    } ?: run {
        DebuggerStatusItem(
            title = contextResources.getString(R.string.appcues_debugger_status_identity_loading_title),
            line1 = contextResources.getString(R.string.appcues_debugger_status_identity_loading_line1),
            statusType = LOADING,
        )
    }

    private fun ArrayList<DebuggerStatusItem>.appendExperienceIfAny() = apply {
        experienceName?.also {
            add(
                DebuggerStatusItem(
                    title = it,
                    line1 = experienceShowingStep,
                    statusType = EXPERIENCE
                )
            )
        }
    }

    suspend fun onTapAction(tapActionType: TapActionType) {
        when (tapActionType) {
            HEALTH_CHECK -> connectToAppcues(true)
        }
    }

    private suspend fun connectToAppcues(update: Boolean) = withContext(Dispatchers.IO) {
        if (connectedToAppcues != null) {
            // set to null and update data so it will update to loading
            connectedToAppcues = null
            if (update) {
                updateData()
            }
            // set new value (true or false) and update data
            connectedToAppcues = appcuesRemoteSource.checkAppcuesConnection()
            updateData()
        }
    }
}
