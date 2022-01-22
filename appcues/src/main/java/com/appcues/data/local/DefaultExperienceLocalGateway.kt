package com.appcues.data.local

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.ExperienceLocalGateway
import java.util.UUID

internal class DefaultExperienceLocalGateway : ExperienceLocalGateway {

    private val cacheMap = HashMap<UUID, Experience>()

    override suspend fun saveExperience(experience: Experience) {
        cacheMap[experience.id] = experience
    }

    override suspend fun getExperienceBy(id: UUID): Experience? {
        return cacheMap[id]
    }
}
