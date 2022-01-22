package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface ExperienceRemoteGateway {

    suspend fun getExperience(contentId: String): Experience?
}
