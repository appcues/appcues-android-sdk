package com.appcues.action

import com.appcues.Appcues
import com.appcues.experience.StepController

interface ExperienceAction {

    val type: String

    suspend fun execute(appcues: Appcues, stepController: StepController, config: HashMap<String, Any>? = null)
}
