package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface CustomerExperienceGateway {

    suspend fun showExperience(experience: Experience)
}
