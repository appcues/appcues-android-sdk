package com.appcues.trait.appcues

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.Experience
import com.appcues.data.model.Step
import com.appcues.data.model.getConfig
import com.appcues.trait.StepGroupingTrait

internal class GroupTrait(override val config: AppcuesConfigMap) : StepGroupingTrait {

    override val groupId: String? = config.getConfig("groupId")

    override fun group(experience: Experience): List<Step> {
        // do the grouping of related steps
        return arrayListOf()
    }
}
