package com.appcues

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.ComposeContainer
import com.appcues.ui.composables.LocalAppcuesActionDelegate
import com.appcues.ui.composables.LocalExperienceStepFormStateDelegate
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.composables.isContentVisible
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

@Composable
fun ComposeContainer(
    context: Context,
    experienceJson: String,
    traitJson: String,
    groupIndex: Int,
    stepIndex: Int,
    imageLoader: ImageLoader)
{
    // set up a Koin scope for testing - for experience/trait mapping, trait registry, etc
    val scope = AppcuesKoinContext.createAppcuesScope(context, AppcuesConfig("", ""))

    // read the JSON test data
    val experienceResponse = MoshiConfiguration.moshi.adapter(ExperienceResponse::class.java).fromJson(experienceJson)!!
    val traitResponse = MoshiConfiguration.moshi.adapter(TraitResponse::class.java).fromJson(traitJson)!!

    // update the experience to add the given trait in the given step
     val updatedExperienceResponse = experienceResponse.copy(
        steps = experienceResponse.steps.mapIndexed { stepContainerResponseIndex, stepContainerResponse ->
            stepContainerResponse.copy(
                children = stepContainerResponse.children.mapIndexed { stepResponseIndex, stepResponse ->
                    stepResponse.copy(
                        traits = stepResponse.traits.toMutableList().also {
                            if (stepContainerResponseIndex == groupIndex && stepResponseIndex == stepIndex) {
                                it.add(traitResponse)
                            }
                        }
                    )
                }
            )
        }
    )

    // map the experience
    val experienceMapper: ExperienceMapper = scope.get()
    val experience = experienceMapper.map(updatedExperienceResponse, ExperienceTrigger.Preview)
    val container = experience.stepContainers[groupIndex]

    // render the step container on the desired step
    isContentVisible.targetState = true // so animated visibility works
    AppcuesTheme {
        CompositionLocalProvider(
            LocalImageLoader provides imageLoader,
            LocalLogcues provides Logcues(LoggingLevel.DEBUG),
            LocalExperienceStepFormStateDelegate provides ExperienceStepFormState(),
            LocalAppcuesActionDelegate provides FakeAppcuesActionDelegate()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ComposeContainer(container, stepIndex)
            }
        }
    }
}

private class FakeAppcuesActionDelegate : AppcuesActionsDelegate {

    override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        // do nothing
    }
}
