package com.appcues.trait

import com.appcues.data.model.Experience
import com.appcues.data.model.Step

internal interface StepGroupingTrait : ExperienceTrait {

    val groupId: String?

    fun group(experience: Experience): List<Step>
}
