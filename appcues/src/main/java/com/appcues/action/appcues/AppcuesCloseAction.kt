package com.appcues.action.appcues

import android.util.Log
import com.appcues.Appcues
import com.appcues.action.ActionConfigMap
import com.appcues.action.ExperienceAction

internal class AppcuesCloseAction(override val config: ActionConfigMap) : ExperienceAction {

    override suspend fun execute(appcues: Appcues) {
        Log.i("Appcues", "AppcuesCloseAction execute")
        // Communicate back to state machine and close experience.
    }
}
