package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface CustomerViewGateway {

    suspend fun showExperience(experience: Experience)
}
