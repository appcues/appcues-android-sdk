package com.appcues.model

import com.appcues.data.model.ExperiencePrimitive
import com.appcues.model.RenderContext.Embed
import com.appcues.model.RenderContext.Modal
import java.util.UUID

internal data class QualifiedExperiences(
    val experiences: List<Experience>,
    val trigger: Trigger
)

internal data class Experience(
    val id: UUID,
    val name: String,
    val traits: List<Trait>,
    val groups: List<Group>,
    val published: Boolean,
    val priority: ExperiencePriority,
    val type: String?,
    val renderContext: RenderContext,
    val publishedAt: Long?,
    val localeId: String?,
    val localeName: String?,
    val experiment: Experiment?,
    val nextContentId: String?,
    val redirectUrl: String?,
    val requestId: UUID?
) {

    var renderErrorId: UUID? = null
}

internal data class Group(
    val id: UUID,
    val children: List<Step>,
    val traits: List<Trait>,
    val actions: Map<UUID, List<Action>>
)

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<Trait>,
    val actions: Map<UUID, List<Action>>,
    val type: String,
)

internal enum class ExperiencePriority {
    LOW, NORMAL
}

internal sealed class Trigger {
    object Event : Trigger()
    object ScreenViewed : Trigger()
    data class ExperienceCompletionAction(val fromExperienceId: UUID?) : Trigger()
    data class LaunchExperienceAction(val fromExperienceId: UUID?) : Trigger()
    object ShowCall : Trigger()
    object DeepLink : Trigger()
    object Preview : Trigger()
}

internal sealed class RenderContext {
    data class Embed(val frameId: String) : RenderContext()
    object Modal : RenderContext()
}

internal fun RenderContext.getFrameId(): String? {
    return when (this) {
        is Embed -> frameId
        Modal -> null
    }
}

internal data class Experiment(
    val id: UUID,
    val group: String,
    val experienceId: UUID,
    val goalId: String,
    val contentType: String,
)

internal data class Trait(
    val type: String,
    val config: Map<String, Any>? = null,
)

internal data class Action(
    val on: String,
    val type: String,
    val config: Map<String, Any>?,
)
