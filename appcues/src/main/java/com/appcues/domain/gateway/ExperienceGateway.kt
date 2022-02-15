package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface ExperienceGateway {

    suspend fun start(experience: Experience)
}
