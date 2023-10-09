package com.appcues.data.local

import com.appcues.data.model.rules.ExperienceRules
import java.util.UUID

internal interface RulesLocalSource {

    suspend fun insert(rules: ExperienceRules)

    suspend fun getViewCount(userId: String, experienceId: UUID): Int
}
