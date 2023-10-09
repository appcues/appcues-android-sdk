package com.appcues.data.local.room

import com.appcues.data.local.RulesLocalSource
import com.appcues.data.local.model.ExperienceRulesEntity
import com.appcues.data.model.rules.ExperienceRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

internal class RoomRulesLocalSource(
    private val db: AppcuesDatabase
) : RulesLocalSource {

    override suspend fun insert(rules: ExperienceRules) = withContext(Dispatchers.IO) {
        db.experienceRulesDao().insert(rules.toExperienceRulesEntity())
    }

    override suspend fun getViewCount(userId: String, experienceId: UUID): Int = withContext(Dispatchers.IO) {
        db.experienceRulesDao().getViewCount(userId, experienceId)
    }

    private fun ExperienceRules.toExperienceRulesEntity(): ExperienceRulesEntity {
        return ExperienceRulesEntity(
            experienceId = experienceId,
            userId = userId,
            seenAt = seenAt
        )
    }
}
