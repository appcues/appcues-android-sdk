package com.appcues

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.logging.Logcues
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.LocalAppcuesActionDelegate
import com.appcues.ui.composables.LocalExperienceStepFormStateDelegate
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.primitive.Compose
import com.appcues.ui.theme.AppcuesTheme

@Composable
fun ComposeContent(json: String, imageLoader: ImageLoader) {
    val response = MoshiConfiguration.moshi.adapter(PrimitiveResponse::class.java).fromJson(json)
    val primitive = response!!.mapPrimitive()

    AppcuesTheme {
        CompositionLocalProvider(
            LocalImageLoader provides imageLoader,
            LocalLogcues provides Logcues(LoggingLevel.DEBUG),
            LocalExperienceStepFormStateDelegate provides ExperienceStepFormState(),
            LocalAppcuesActionDelegate provides FakeAppcuesActionDelegate()
        ) {
            primitive.Compose()
        }
    }
}

private class FakeAppcuesActionDelegate : AppcuesActionsDelegate {

    override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        // do nothing
    }
}
