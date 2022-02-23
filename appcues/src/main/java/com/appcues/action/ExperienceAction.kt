package com.appcues.action

import com.appcues.Appcues

interface ExperienceAction {

    val config: HashMap<String, Any>?

    suspend fun execute(appcues: Appcues)
}
