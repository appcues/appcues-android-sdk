package com.appcues.debugger

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import com.appcues.AppcuesConfig
import com.appcues.BuildConfig
import com.appcues.R
import com.appcues.Storage
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.StatusType.ERROR
import com.appcues.debugger.model.StatusType.EXPERIENCE
import com.appcues.debugger.model.StatusType.LOADING
import com.appcues.debugger.model.StatusType.PHONE
import com.appcues.debugger.model.StatusType.SUCCESS
import com.appcues.debugger.model.StatusType.UNKNOWN
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.model.TapActionType.DEEPLINK_CHECK
import com.appcues.debugger.model.TapActionType.HEALTH_CHECK
import com.appcues.util.ContextWrapper
import com.appcues.util.resolveActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
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
    storage: Storage,
    private val appcuesConfig: AppcuesConfig,
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val contextWrapper: ContextWrapper,
    private val context: Context,
    private val analyticsTracker: AnalyticsTracker,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var connectedToAppcues: Boolean? = false

    private var deepLinkConfigured: Boolean? = null
    private var deepLinkValidationToken: String? = null
    private var deepLinkErrorText: String? = null

    private var trackingScreens: Boolean? = null

    private var userIdentified: String? = storage.userId.ifEmpty { null }

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

        launch { connectToAppcues(false) }

        isStarted = true
    }

    fun reset() {
        isStarted = false
        userIdentified = null
        coroutineContext.cancelChildren()
        launch { updateData() }
    }

    private suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        userIdentified = activityRequest.userId.ifEmpty { null }

        activityRequest.events?.forEach { event ->
            when (event.name) {
                AnalyticsEvent.ScreenView.eventName -> {
                    trackingScreens = true
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
        return if (deepLinkPath == deepLinkValidationToken) {
            deepLinkConfigured = true
            deepLinkValidationToken = null
            updateData()
            true
        } else {
            false
        }
    }

    private suspend fun updateData() {
        _data.emit(
            arrayListOf(
                deviceInfoItem(),
                sdkInfoItem(),
                connectionCheckItem(),
                deepLinkCheckItem(),
                trackingScreenCheckItem(),
                identityItem()
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

    private fun connectionCheckItem() = (connectedToAppcues?.let { if (it) SUCCESS else ERROR } ?: LOADING).let { statusType ->
        DebuggerStatusItem(
            title = statusType.let {
                when (it) {
                    SUCCESS -> R.string.appcues_debugger_status_check_connection_connected_title
                    LOADING -> R.string.appcues_debugger_status_check_connection_connecting_title
                    else -> R.string.appcues_debugger_status_check_connection_error_title
                }
            }.let { contextWrapper.getString(it) },
            line1 = statusType.let {
                when (it) {
                    ERROR -> R.string.appcues_debugger_status_check_connection_error_line1
                    else -> null
                }
            }?.let { contextWrapper.getString(it) },
            statusType = statusType,
            showRefreshIcon = statusType != LOADING,
            tapActionType = HEALTH_CHECK
        )
    }

    private fun deepLinkCheckItem() = (deepLinkConfigured?.let { if (it) SUCCESS else ERROR } ?: UNKNOWN).let { statusType ->
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_title),
            line1 = statusType.let {
                when (it) {
                    UNKNOWN -> contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_instruction)
                    ERROR -> deepLinkErrorText
                    else -> null
                }
            },
            statusType = statusType,
            showRefreshIcon = statusType == UNKNOWN,
            tapActionType = DEEPLINK_CHECK
        )
    }

    private fun trackingScreenCheckItem() = (trackingScreens?.let { if (it) SUCCESS else ERROR } ?: LOADING).let { statusType ->
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_check_screen_tracking_title),
            line1 = statusType.let {
                when (it) {
                    LOADING -> R.string.appcues_debugger_status_check_screen_tracking_loading_line1
                    else -> null
                }
            }?.let { contextWrapper.getString(it) },
            statusType = statusType,
        )
    }

    private fun identityItem() = userIdentified?.let {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_identity_success_title),
            line1 = userIdentified,
            statusType = SUCCESS,
        )
    } ?: run {
        DebuggerStatusItem(
            title = contextWrapper.getString(R.string.appcues_debugger_status_identity_loading_title),
            line1 = contextWrapper.getString(R.string.appcues_debugger_status_identity_loading_line1),
            statusType = LOADING,
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
            HEALTH_CHECK -> connectToAppcues(true)
            DEEPLINK_CHECK -> checkDeepLinkIntentFilter()
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

    private suspend fun checkDeepLinkIntentFilter() {
        deepLinkErrorText = null

        if (deepLinkConfigured != true) {

            // set to null and update data so it will update to loading
            if (deepLinkConfigured != null) {
                deepLinkConfigured = null
                updateData()
            }

            val token = "verify-${UUID.randomUUID()}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("appcues-${appcuesConfig.applicationId}://sdk/debugger/$token")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val manifestConfigured = context.packageManager.resolveActivityCompat(intent, PackageManager.MATCH_DEFAULT_ONLY) != null

            if (manifestConfigured) {
                deepLinkValidationToken = token
                context.startActivity(intent)

                // a new link should have come in and updated our state in the checkDeepLinkValidation function above
                // we give that a little time to process
                delay(1.seconds)

                // if after 1 second we still do not have that updated state - it failed
                if (deepLinkValidationToken != null) {
                    deepLinkValidationToken = null
                    deepLinkConfigured = false
                    deepLinkErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_error_handler)
                    updateData()
                }
            } else {
                deepLinkConfigured = false
                deepLinkErrorText = contextWrapper.getString(R.string.appcues_debugger_status_check_deep_link_error_manifest)
                updateData()
            }
        }
    }
}
