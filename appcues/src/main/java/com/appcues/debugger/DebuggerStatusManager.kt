package com.appcues.debugger

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.DeepLinkHandler
import com.appcues.R
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.remote.RemoteError.HttpErrorV2
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.StatusType
import com.appcues.debugger.model.StatusType.ERROR
import com.appcues.debugger.model.StatusType.EXPERIENCE
import com.appcues.debugger.model.StatusType.IDLE
import com.appcues.debugger.model.StatusType.LOADING
import com.appcues.debugger.model.StatusType.PHONE
import com.appcues.debugger.model.StatusType.SUCCESS
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.model.TapActionType.DEEPLINK_CHECK
import com.appcues.debugger.model.TapActionType.HEALTH_CHECK
import com.appcues.debugger.model.TapActionType.OPEN_SETTINGS
import com.appcues.debugger.model.TapActionType.PUSH_CHECK
import com.appcues.util.ContextWrapper
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions")
internal class DebuggerStatusManager(
    private val storage: Storage,
    private val appcuesConfig: AppcuesConfig,
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val contextWrapper: ContextWrapper,
    private val analyticsTracker: AnalyticsTracker,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var connectionStatus: StatusType = IDLE

    private var pushStatus: StatusType = IDLE
    private var pushTapAction: TapActionType = PUSH_CHECK
    private var pushValidationToken: String? = null
    private var pushErrorText: String? = null
    private var pushTimeoutJob: Job? = null

    private var deeplinkStatus: StatusType = IDLE
    private var deepLinkValidationToken: String? = null
    private var deepLinkErrorText: String? = null

    private var screenTrackingStatus: StatusType = LOADING

    private var userId: String? = storage.userId.ifEmpty { null }
    private var groupId: String? = storage.groupId

    private data class DisplayingExperience(
        val name: String,
        val frameId: String? = null,
        var step: String? = null
    )

    private val displayingExperiences = hashMapOf<String, DisplayingExperience>()

    private val _data = MutableStateFlow<List<DebuggerStatusItem>>(arrayListOf())

    val data: StateFlow<List<DebuggerStatusItem>>
        get() = _data

    private var isStarted = false

    fun start() {
        if (isStarted) return

        launch {
            analyticsTracker.analyticsFlow.collect {
                onActivityRequest(it.request)
            }
        }

        launch { checkConnection() }

        isStarted = true
    }

    fun reset() {
        isStarted = false
        userId = null
        groupId = null
        coroutineContext.cancelChildren()
        launch { updateData() }
    }

    private suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        userId = activityRequest.userId.ifEmpty { null }
        groupId = activityRequest.groupId

        activityRequest.events?.forEach { event ->
            when (event.name) {
                AnalyticsEvent.ScreenView.eventName -> {
                    screenTrackingStatus = SUCCESS
                }
                AnalyticsEvent.ExperienceStarted.eventName -> {
                    val id = event.attributes["experienceId"] as String
                    displayingExperiences[id] = DisplayingExperience(
                        name = contextWrapper.getString(
                            R.string.appcues_debugger_status_experience_name,
                            event.attributes["experienceName"] as String
                        ),
                        frameId = event.attributes["frameId"] as String?,
                    )
                }
                AnalyticsEvent.ExperienceStepSeen.eventName -> {
                    val id = event.attributes["experienceId"] as String
                    val step = (event.attributes["stepIndex"] as String).split(",").let {
                        val group = it.first().toInt() + 1
                        val step = it.last().toInt() + 1

                        contextWrapper.getString(R.string.appcues_debugger_status_experience_step, group, step)
                    }

                    displayingExperiences[id]?.step = step
                }
                AnalyticsEvent.ExperienceCompleted.eventName, AnalyticsEvent.ExperienceDismissed.eventName -> {
                    val id = event.attributes["experienceId"] as String

                    displayingExperiences.remove(id)
                }
                else -> Unit
            }
        }

        updateData()
    }

    suspend fun checkDeepLinkValidation(deepLinkPath: String): Boolean {
        return when (deepLinkPath) {
            deepLinkValidationToken -> {
                deeplinkStatus = SUCCESS
                deepLinkValidationToken = null
                updateData()
                true
            }
            pushValidationToken -> {
                pushStatus = SUCCESS
                pushTimeoutJob?.cancel()
                pushValidationToken = null
                updateData()
                true
            }
            else -> false
        }
    }

    private suspend fun updateData() {
        _data.emit(
            arrayListOf(
                deviceInfoItem(),
                sdkInfoItem(),
                connectionCheckItem(),
                deepLinkCheckItem(),
                pushCheckItem(),
                trackingScreenCheckItem(),
                identifyUserItem(),
                identifyGroupItem()
            ).appendExperienceIfAny()
        )
    }

    private fun deviceInfoItem() = DebuggerStatusItem(
        title = contextWrapper.getString(R.string.appcues_debugger_status_device_title, Build.MANUFACTURER, VERSION.RELEASE),
        statusType = PHONE,
    )

    private fun sdkInfoItem() = DebuggerStatusItem(
        title = contextWrapper.getString(R.string.appcues_debugger_status_sdk_title, BuildConfig.SDK_VERSION),
        statusType = SUCCESS,
        line1 = contextWrapper.getString(R.string.appcues_debugger_status_sdk_line1, appcuesConfig.accountId),
        line2 = contextWrapper.getString(R.string.appcues_debugger_status_sdk_line2, appcuesConfig.applicationId)
    )

    private fun connectionCheckItem() = DebuggerStatusItem(
        title = when (connectionStatus) {
            SUCCESS -> contextWrapper.getString(R.string.appcues_debugger_status_check_connection_connected_title)
            LOADING -> contextWrapper.getString(R.string.appcues_debugger_status_check_connection_connecting_title)
            else -> contextWrapper.getString(R.string.appcues_debugger_status_check_connection_error_title)
        },
        line1 = when (connectionStatus) {
            ERROR -> contextWrapper.getString(R.string.appcues_debugger_status_check_connection_error_line1)
            else -> null
        },
        statusType = connectionStatus,
        showRefreshIcon = connectionStatus != LOADING,
        tapActionType = HEALTH_CHECK
    )

    private fun deepLinkCheckItem() = DebuggerStatusItem(
        title = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_title),
        line1 = deeplinkStatus.let {
            when (it) {
                IDLE -> contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_instruction)
                ERROR -> deepLinkErrorText
                else -> null
            }
        },
        statusType = deeplinkStatus,
        showRefreshIcon = deeplinkStatus != LOADING,
        tapActionType = DEEPLINK_CHECK
    )

    private fun pushCheckItem() = DebuggerStatusItem(
        title = contextWrapper.getString(R.string.appcues_debugger_status_check_push_title),
        line1 = when (pushStatus) {
            IDLE -> contextWrapper.getString(R.string.appcues_debugger_status_check_push_instruction)
            LOADING -> contextWrapper.getString(R.string.appcues_debugger_status_check_push_loading_instruction)
            ERROR -> pushErrorText
            else -> null
        },
        statusType = pushStatus,
        showRefreshIcon = pushStatus != LOADING,
        tapActionType = pushTapAction
    )

    private fun trackingScreenCheckItem() = DebuggerStatusItem(
        title = contextWrapper.getString(R.string.appcues_debugger_status_check_screen_tracking_title),
        line1 = screenTrackingStatus.let {
            when (it) {
                LOADING -> R.string.appcues_debugger_status_check_screen_tracking_loading_line1
                else -> null
            }
        }?.let { contextWrapper.getString(it) },
        statusType = screenTrackingStatus,
    )

    private fun identifyUserItem() = userId?.let {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_user_identity_title),
            line1 = it,
            statusType = SUCCESS,
        )
    } ?: run {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_no_user_identity_title),
            line1 = contextWrapper.getString(R.string.appcues_debugger_status_no_user_identity_description),
            statusType = LOADING,
        )
    }

    private fun identifyGroupItem() = groupId?.let {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_group_identity_title),
            line1 = it,
            statusType = SUCCESS,
        )
    } ?: run {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_no_group_identity_title),
            statusType = IDLE,
        )
    }

    private fun ArrayList<DebuggerStatusItem>.appendExperienceIfAny() = apply {
        displayingExperiences.values.forEach {
            add(
                DebuggerStatusItem(
                    title = it.name,
                    line1 = it.step + (it.frameId?.let { frame -> " ($frame)" } ?: String()),
                    statusType = EXPERIENCE
                )
            )
        }
    }

    suspend fun onTapAction(tapActionType: TapActionType) {
        when (tapActionType) {
            HEALTH_CHECK -> checkConnection()
            DEEPLINK_CHECK -> checkDeeplink()
            PUSH_CHECK -> checkPush()
            OPEN_SETTINGS -> openSettings()
        }
    }

    private suspend fun openSettings() {
        pushStatus = IDLE
        pushTapAction = PUSH_CHECK
        pushErrorText = null
        updateData()

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("package", contextWrapper.getPackageName(), null)
        }
        contextWrapper.startIntent(intent)
    }

    private suspend fun checkConnection() {
        // set to null and update data so it will update to loading
        connectionStatus = LOADING
        updateData()

        withContext(Dispatchers.IO) {
            // set new value (true or false) and update data
            connectionStatus = if (appcuesRemoteSource.checkAppcuesConnection()) SUCCESS else ERROR
            updateData()
        }
    }

    private suspend fun checkDeeplink() {
        // set to null and update data so it will update to loading
        deeplinkStatus = LOADING
        deepLinkErrorText = null
        val token = "test-deeplink-${UUID.randomUUID()}"
        deepLinkValidationToken = token
        updateData()

        val scheme = contextWrapper.getString(R.string.appcues_custom_scheme).ifEmpty { "appcues-${appcuesConfig.applicationId}" }
        val intent = DeepLinkHandler.getDebuggerValidationIntent(scheme, token)
        if (contextWrapper.isIntentSupported(intent)) {
            contextWrapper.startIntent(intent)

            // a new link should have come in and updated our state in the checkDeepLinkValidation function above
            // we give that a little time to process
            delay(1.seconds)

            // if after 1 second we still do not have that updated state - it failed
            if (deepLinkValidationToken != null) {
                deepLinkValidationToken = null
                deeplinkStatus = ERROR
                deepLinkErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_error_handler)
                updateData()
            }
        } else {
            deepLinkValidationToken = null
            deeplinkStatus = ERROR
            deepLinkErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_error_manifest)
            updateData()
        }
    }

    private suspend fun checkPush() {
        if (!isLocalPushSetup()) {
            updateData()
            return
        }

        // set to null and update data so it will update to loading
        pushTimeoutJob?.cancel()
        pushErrorText = null
        pushValidationToken = null
        pushStatus = LOADING
        updateData()

        // create new random id and send it to the server
        val token = "test-push-${UUID.randomUUID()}"
        when (val result = appcuesRemoteSource.checkAppcuesPush(token)) {
            is Failure -> {
                val reason = result.reason
                pushErrorText = if (reason is HttpErrorV2 && reason.error != null) {
                    contextWrapper.getString(R.string.appcues_debugger_status_check_push_server_error, reason.error.error)
                } else {
                    contextWrapper.getString(R.string.appcues_debugger_status_check_push_unknown_server_error)
                }
                pushStatus = ERROR
                updateData()
            }
            is Success -> {
                pushValidationToken = token

                // we wait for certain amount of time before we say that the notification was ignored/dismissed or maybe not presented for some reason
                coroutineScope {
                    pushTimeoutJob = launch {
                        delay(timeMillis = 30_000)
                        if (pushStatus == LOADING) {
                            pushStatus = ERROR
                            pushErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_push_error_ignored)
                            updateData()
                        }
                    }
                }
            }
        }
    }

    // run possible local checks regarding to push setup
    private fun isLocalPushSetup(): Boolean {
        if (!contextWrapper.isNotificationEnabled()) {
            pushErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_push_error_no_permission)
            pushStatus = ERROR
            pushTapAction = OPEN_SETTINGS
            return false
        }

        if (storage.pushToken == null) {
            pushErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_push_error_no_token)
            pushStatus = ERROR
            return false
        }

        return true
    }
}
