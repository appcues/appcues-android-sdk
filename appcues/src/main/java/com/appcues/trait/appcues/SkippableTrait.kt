package com.appcues.trait.appcues

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcues.AppcuesCoroutineScope
import com.appcues.R
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class SkippableTrait(
    override val config: AppcuesConfigMap,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) : ContainerDecoratingTrait, BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/skippable"
    }

    override val priority: Int = BackdropDecoratingTrait.BACKDROP_SKIPPABLE_PRIORITY

    override val containerComposeOrder = ContainerDecoratingType.OVERLAY

    @Composable
    override fun BoxScope.DecorateContainer() {
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .size(30.dp, 30.dp),
            onClick = {
                appcuesCoroutineScope.launch {
                    experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)
                }
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.appcues_ic_experience_dismiss),
                contentDescription = stringResource(id = R.string.appcues_skippable_trait_dismiss),
            )
        }
    }

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                // add click listener but without any ripple effect, should this happen?
                .pointerInput(Unit) {
                    detectTapGestures {
                        appcuesCoroutineScope.launch {
                            experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)
                        }
                    }
                },
        )

        content()
    }
}
