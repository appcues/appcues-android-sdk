package com.appcues.rendering

import com.appcues.action.ExperienceAction
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.StepContainer
import com.appcues.model.RenderContext.Embed
import com.appcues.model.RenderContext.Modal
import java.util.UUID

internal data class ComposedExperience(

    val id: UUID,
    val name: String,
    val groups: List<StepContainer>,
    val published: Boolean,
    val priority: com.appcues.data.model.ExperiencePriority,
    val type: String?,
    val renderContext: com.appcues.data.model.RenderContext,
    val publishedAt: Long?,
    val localeId: String?,
    val localeName: String?,
    val experiment: com.appcues.data.model.Experiment?,
    val completionActions: List<ExperienceAction>,
    val trigger: ExperienceTrigger,
    val requestId: UUID? = null,
    val error: String? = null,
    var renderErrorId: UUID? = null,

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
    val requestId: UUID?,
)

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
    object Event: Trigger()
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
