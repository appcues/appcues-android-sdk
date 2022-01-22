package com.appcues.data.remote

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.ExperienceRemoteGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class MockExperienceRemoteGateway : ExperienceRemoteGateway {

    override suspend fun getExperience(contentId: String): Experience = withContext(Dispatchers.IO) {
        return@withContext experienceModalOne
    }
}
