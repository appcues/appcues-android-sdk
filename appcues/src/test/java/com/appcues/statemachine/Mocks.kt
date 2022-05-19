package com.appcues.statemachine

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import java.util.UUID

internal class TestPresentingTrait(private val onPresent: (() -> Unit)?) : PresentingTrait {
    override val config: Map<String, Any>? = null
    override fun present() { onPresent?.invoke() }
}

internal class TestContentHolderTrait : ContentHolderTrait {
    override val config: Map<String, Any>? = null
    @Composable
    @Suppress("EmptyFunctionBlock")
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) { }
}

internal class TestContentWrappingTrait : ContentWrappingTrait {
    override val config: Map<String, Any>? = null
    @Composable
    @Suppress("EmptyFunctionBlock")
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) { }
}

// an experience with a group having 3 steps, then a single step group
internal fun mockExperience(onPresent: (() -> Unit)? = null) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience",
        type = "mobile",
        stepContainers = listOf(
            StepContainer(
                steps = listOf(
                    mockStep,
                    mockStep,
                    mockStep,
                ),
                presentingTrait = TestPresentingTrait(onPresent),
                contentHolderTrait = TestContentHolderTrait(),
                contentWrappingTrait = TestContentWrappingTrait(),
                backdropTraits = listOf(),
                containerTraits = listOf(),
            ),
            StepContainer(
                steps = listOf(
                    mockStep,
                ),
                presentingTrait = TestPresentingTrait(onPresent),
                contentHolderTrait = TestContentHolderTrait(),
                contentWrappingTrait = TestContentWrappingTrait(),
                backdropTraits = listOf(),
                containerTraits = listOf(),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000
    )

internal val mockStep =
    Step(
        id = UUID.fromString("6277c1b1-4bd3-4158-ae17-a888a8abc6d9"),
        content = TextPrimitive(
            id = UUID.fromString("df3bbe3e-8bdb-417a-b644-5e23862786b2"),
            text = ""
        ),
        traits = listOf(),
        actions = mapOf(),
        type = "modal"
    )
