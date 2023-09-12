package com.appcues.experiences

import com.appcues.analytics.RenderingService
import com.appcues.analytics.RenderingService.PreviewExperienceResult
import com.appcues.analytics.RenderingService.ShowExperienceResult
import com.appcues.analytics.SessionService
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.ExperienceTrigger.Preview
import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Experiences(
    private val remote: AppcuesRemoteSource,
    private val experienceMapper: ExperienceMapper,
    private val sessionService: SessionService,
    private val renderingService: RenderingService,
) {

    companion object {

        private const val HTTP_CODE_NOT_FOUND = 404
    }

    suspend fun show(experienceId: String, trigger: ExperienceTrigger): ShowExperienceResult = withContext(Dispatchers.IO) {
        // cannot show if no session is active
        val sessionProperties = sessionService.getSessionProperties() ?: return@withContext ShowExperienceResult.NoSession

        return@withContext remote.getExperienceContent(experienceId, sessionProperties.userSignature).run {
            when (this) {
                is Success -> renderingService.show(experienceMapper.map(value, trigger))
                is Failure -> if (reason is HttpError && reason.code == HTTP_CODE_NOT_FOUND)
                    ShowExperienceResult.ExperienceNotFound else ShowExperienceResult.Error(reason.toString())
            }
        }
    }

    suspend fun preview(experienceId: String): PreviewExperienceResult = withContext(Dispatchers.IO) {
        val sessionProperties = sessionService.getSessionProperties()

        return@withContext remote.getExperiencePreview(experienceId, sessionProperties?.userSignature).run {
            when (this) {
                is Success -> renderingService.preview(experienceMapper.map(value, Preview))
                is Failure -> if (reason is HttpError && reason.code == HTTP_CODE_NOT_FOUND)
                    PreviewExperienceResult.ExperienceNotFound else PreviewExperienceResult.Error(reason.toString())
            }
        }
    }
}
