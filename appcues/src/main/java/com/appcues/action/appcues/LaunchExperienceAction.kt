package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.getConfig
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import java.util.UUID

internal class LaunchExperienceAction(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine,
    private val experienceRenderer: ExperienceRenderer,
) : ExperienceAction, MetadataSettingsAction {

    // this constructor is called to create an instance of this action from the post-flow-completion
    // actions that are defined in flow settings, that execute after the flow (not a button action)
    constructor(
        completedExperienceId: String,
        launchExperienceId: String,
        stateMachine: StateMachine,
        experienceRenderer: ExperienceRenderer,
    ) : this(
        config = hashMapOf<String, Any>(
            "completedExperienceID" to completedExperienceId,
            "experienceID" to launchExperienceId,
        ),
        stateMachine = stateMachine,
        experienceRenderer = experienceRenderer
    )

    companion object {

        const val TYPE = "@appcues/launch-experience"
    }

    private val experienceId: String? = config.getConfig("experienceID")
    private val completedExperienceId: String? = config.getConfig("completedExperienceID")

    override val category = "internal"

    override val destination = experienceId ?: String()

    override suspend fun execute() {
        if (experienceId != null) {
            experienceRenderer.show(experienceId, getTrigger())
        }
    }

    private fun getTrigger() =
        if (completedExperienceId != null) {
            // if a completed experience ID was provided - this means the action originated as a post-flow
            // completion action, so this should be supplied as the trigger type
            ExperienceTrigger.ExperienceCompletionAction(UUID.fromString(completedExperienceId))
        } else {
            // more typical case - the action was a button action within a flow that is launching another
            // flow - capture the current experience from the state machine as the experience that is launching the new flow.
            // note: it's possible that the current experience was closed out before this triggered, in which case this
            // fromExperience ID value would be null.
            val fromExperience = stateMachine.state.currentExperience
            ExperienceTrigger.LaunchExperienceAction(fromExperience?.id)
        }
}
