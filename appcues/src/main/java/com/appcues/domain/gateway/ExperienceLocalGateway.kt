package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience
import java.util.UUID

internal interface ExperienceLocalGateway {

    suspend fun saveExperience(experience: Experience)

    suspend fun getExperienceBy(id: UUID): Experience?
}
