package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface ExperienceGateway {

    suspend fun getExperiences(contentId: String): List<Experience>
}
