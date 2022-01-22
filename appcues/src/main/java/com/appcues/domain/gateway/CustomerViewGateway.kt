package com.appcues.domain.gateway

import java.util.UUID

internal interface CustomerViewGateway {

    suspend fun showExperience(experienceId: UUID)
}
