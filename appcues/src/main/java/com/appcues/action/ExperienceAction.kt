package com.appcues.action

import com.appcues.Appcues

interface ExperienceAction {

    val config: HashMap<String, Any>?

    // todo - maybe does not need to be suspend function
    suspend fun execute(appcues: Appcues)
}
