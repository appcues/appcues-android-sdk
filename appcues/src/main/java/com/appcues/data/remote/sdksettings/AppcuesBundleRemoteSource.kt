package com.appcues.data.remote.sdksettings

import com.appcues.AppcuesConfig
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.rules.RulesMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.LOW
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.LocalQualification
import com.appcues.data.model.QualifiableExperience
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.experience.FailedExperienceResponse
import com.appcues.data.remote.appcues.response.experience.LossyExperienceResponse
import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse
import com.appcues.trait.AppcuesTraitException
import com.appcues.util.ResultOf
import com.appcues.util.doIfSuccess

internal class AppcuesBundleRemoteSource(
    private val service: BundleService,
    private val config: AppcuesConfig,
    private val experienceMapper: ExperienceMapper,
    private val ruleMapper: RulesMapper,
) {

    companion object {

        const val BASE_URL = "https://fast.appcues.com/"
    }

    suspend fun sdkSettings(): ResultOf<SdkSettingsResponse, RemoteError> =
        NetworkRequest.execute {
            service.sdkSettings(config.accountId)
        }

    suspend fun getLocalQualification(accountId: String): LocalQualification {
        val result = NetworkRequest.execute {
            service.getLocalQualifications(accountId)
        }
        val qualifiableExperiences = arrayListOf<QualifiableExperience>()

        result.doIfSuccess { response ->
            response.qualifications.mapNotNull {
                val experience = it.experience.mapToExperience() ?: return@mapNotNull null
                val rule = ruleMapper.map(it.rule) ?: return@mapNotNull null
                QualifiableExperience(
                    experience = experience,
                    rule = rule,
                    sortingPriority = it.sortPriority
                )
            }.forEach {
                qualifiableExperiences.add(it)
            }
        }

        return LocalQualification(qualifiableExperiences)
    }

    private fun LossyExperienceResponse.mapToExperience(): Experience? {
        return try {
            experienceMapper.mapDecoded(this, Qualification("offline"), LOW)
        } catch (ex: AppcuesTraitException) {
            when (this) {
                is ExperienceResponse -> {
                    // when a trait exception occurs, that means an otherwise valid response had some invalid trait
                    // configuration - we wrap that in a failed response and map it, so it can be reported, and then
                    // any subsequent lower priority experiences can be attempted
                    val failed = FailedExperienceResponse(
                        id = id,
                        name = name,
                        type = type,
                        publishedAt = publishedAt,
                        context = context,
                        error = ex.message
                    )
                    experienceMapper.mapDecoded(failed, Qualification("offline"), LOW)
                }
                // it is not expected that a failed response can throw in any way, so should not get here
                is FailedExperienceResponse -> null
            }
        }
    }
}
