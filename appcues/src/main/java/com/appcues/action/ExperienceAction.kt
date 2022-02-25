package com.appcues.action

import com.appcues.Appcues
import com.appcues.data.model.AppcuesConfigMap

interface ExperienceAction {

    val config: AppcuesConfigMap

    suspend fun execute(appcues: Appcues)
}
