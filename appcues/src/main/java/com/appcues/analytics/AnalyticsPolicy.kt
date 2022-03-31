package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.logging.Logcues
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

internal class AnalyticsPolicy(
    private val sessionMonitor: SessionMonitor,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val stateMachine: StateMachine,
    private val logcues: Logcues,
) {

    private var lastScreen: String? = null
    private var activeExperienceScreen: String? = null
    private var experienceActiveCount = 0

    private val isExperienceActive: Boolean
        get() = experienceActiveCount > 0

    init {
        appcuesCoroutineScope.launch {
            stateMachine.stateFlow.collect {
                when (it) {
                    is BeginningExperience -> {
                        experienceActiveCount++
                        activeExperienceScreen = lastScreen
                    }
                    is EndingExperience -> {
                        experienceActiveCount--

                        // this is to handle the edge case of
                        // (1) screen view qualifies an experience
                        // (2) experience completes and returns to customer activity
                        // (3) app is backgrounded
                        // (4) app is foregrounded
                        // after (4) the next onResume should be allowed to track the screen
                        // so if sufficient time has passed (1 sec) to avoid the initial onResume re-trigger of screen
                        // this will clear out the activeExperienceScreen and allow screen events to flow again with
                        // the same name.
                        //
                        // this is needed, since after (2) the onResume() happens _before_ the state machine
                        // fully transitions to EndingExperience here.
                        Timer().schedule(1) {
                            activeExperienceScreen = null
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun canIdentify() = basicAnalyticsPolicy("unable to track user")

    fun canTrackEvent() = basicAnalyticsPolicy("unable to track event")

    fun canTrackScreen(title: String): Boolean {
        if (!basicAnalyticsPolicy("unable to track screen")) return false

        // Avoid reporting the same screen view as a duplicate to what was reported when an
        // experience was triggered.
        // This is to avoid the infinite loop of onResume() triggering a "show every time"
        // experience, when an experience activity ends and the customer activity resumes.
        if (activeExperienceScreen != null) {
            val skip = title == activeExperienceScreen

            // If an experience is still active (event if in process of closing) maintain the
            // activeExperienceScreen with the current item title for next screen check.
            //
            // Otherwise, set it to null, so that normal screen tracking can resume
            activeExperienceScreen = if (isExperienceActive) title else null

            if (skip) {
                logcues.info("skipping duplicate screen tracking after experience end")
                return false
            }
        }

        lastScreen = title
        return true
    }

    fun canTrackGroup() = basicAnalyticsPolicy("unable to track group")

    private fun basicAnalyticsPolicy(message: String) = sessionMonitor.checkSession(message)
}
