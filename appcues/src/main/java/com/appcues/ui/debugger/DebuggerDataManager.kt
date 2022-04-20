package com.appcues.ui.debugger

import android.os.Build
import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.ui.debugger.StatusType.ERROR
import com.appcues.ui.debugger.StatusType.EXPERIENCE
import com.appcues.ui.debugger.StatusType.LOADING
import com.appcues.ui.debugger.StatusType.PHONE
import com.appcues.ui.debugger.StatusType.SUCCESS
import com.appcues.ui.debugger.TapActionType.HEALTH_CHECK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

internal class DebuggerDataManager(
    appcuesConfig: AppcuesConfig,
    storage: Storage,
    private val appcuesRemoteSource: AppcuesRemoteSource,
) {

    private val deviceName: String = "(${Build.MANUFACTURER}) Android ${Build.VERSION.RELEASE}"

    private val accountId: String = appcuesConfig.accountId

    private val applicationId: String = appcuesConfig.applicationId

    private var connectedToAppcues: Boolean? = false

    private var trackingScreens: Boolean? = null

    private var userIdentified: String? = storage.userId.let { it.ifEmpty { null } }

    private var experienceName: String? = null
    private var experienceShowingStep: String? = null

    private val _data = MutableStateFlow<List<DebuggerStatusItem>>(arrayListOf())

    val data: StateFlow<List<DebuggerStatusItem>>
        get() = _data

    suspend fun start() = withContext(Dispatchers.IO) {
        connectToAppcues(false)
        trackingScreens = null
        updateData()
    }

    suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        userIdentified = activityRequest.userId

        activityRequest.events?.forEach {
            when (it.name) {
                AnalyticsEvent.ScreenView.eventName -> {
                    trackingScreens = true
                }
                AnalyticsEvent.ExperienceStarted.eventName -> {
                    experienceName = it.attributes["experienceName"] as String
                }
                AnalyticsEvent.ExperienceStepSeen.eventName -> {
                    val group = (it.attributes["groupIndex"] as Int) + 1
                    val step = (it.attributes["stepIndexInGroup"] as Int) + 1
                    experienceShowingStep = "Showing group $group step $step"
                }
                AnalyticsEvent.ExperienceCompleted.eventName -> {
                    experienceName = null
                    experienceShowingStep = null
                }
                AnalyticsEvent.ExperienceDismissed.eventName -> {
                    experienceName = null
                    experienceShowingStep = null
                }
            }
        }

        activityRequest.events?.any { it.name == AnalyticsEvent.ScreenView.eventName }?.let { trackingScreens = true }

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
        title = deviceName,
        statusType = PHONE,
    )

    private fun sdkInfoItem() = DebuggerStatusItem(
        title = "Installed SDK",
        statusType = SUCCESS,
        line1 = "Account ID: $accountId",
        line2 = "Application ID: $applicationId"
    )

    private fun connectionCheckItem() = DebuggerStatusItem(
        title = "Connected to Appcues",
        statusType = connectedToAppcues?.let { if (it) SUCCESS else ERROR } ?: LOADING,
        showRefreshIcon = connectedToAppcues?.let { true } ?: false,
        tapActionType = HEALTH_CHECK
    )

    private fun trackingScreenCheckItem() = DebuggerStatusItem(
        title = "Tracking Screens",
        line1 = if (trackingScreens?.let { true } == true) null else "waiting for screen event...",
        statusType = trackingScreens?.let { if (it) SUCCESS else ERROR } ?: LOADING,
    )

    private fun identityItem() = DebuggerStatusItem(
        title = userIdentified?.let { "User Identified" } ?: "Identifying User",
        line1 = userIdentified?.let { userIdentified } ?: "waiting for any event...",
        statusType = userIdentified?.let { SUCCESS } ?: LOADING,
    )

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
