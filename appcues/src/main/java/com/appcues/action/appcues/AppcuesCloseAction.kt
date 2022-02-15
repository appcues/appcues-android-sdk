package com.appcues.action.appcues

import android.util.Log
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.experience.StepController

internal class AppcuesCloseAction : ExperienceAction {

    override val type: String
        get() = "@appcues/close"

    override suspend fun execute(appcues: Appcues, stepController: StepController, config: HashMap<String, Any>?) {
        Log.i("Appcues", "AppcuesCloseAction execute")
        stepController.end()
    }
}
