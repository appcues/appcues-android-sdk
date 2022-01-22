package com.appcues.domain

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.ExperienceLocalGateway
import java.util.UUID

internal class GetExperienceUseCase(
    val experienceLocal: ExperienceLocalGateway
) {

    suspend operator fun invoke(id: UUID): Experience? {
        return experienceLocal.getExperienceBy(id)
    }
}
