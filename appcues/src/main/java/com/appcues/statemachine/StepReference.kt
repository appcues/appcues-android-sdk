package com.appcues.statemachine

import com.appcues.data.model.Experience
import java.util.UUID

internal sealed class StepReference {

    abstract fun getIndex(experience: Experience, currentStepIndex: Int): Int?

    data class StepId(val id: UUID) : StepReference() {

        override fun getIndex(experience: Experience, currentStepIndex: Int): Int? {
            experience.flatSteps.forEachIndexed { index, step ->
                // return index if matches any step in experience
                if (step.id == id) return index
            }

            return null
        }
    }

    data class StepIndex(val index: Int) : StepReference() {

        override fun getIndex(experience: Experience, currentStepIndex: Int): Int? {
            // just return index
            return validateStepIndexOrNull(experience, index)
        }
    }

    data class StepOffset(val offset: Int) : StepReference() {

        override fun getIndex(experience: Experience, currentStepIndex: Int): Int? {
            // apply offset to current step index
            return validateStepIndexOrNull(experience, currentStepIndex + offset)
        }
    }

    protected fun validateStepIndexOrNull(experience: Experience, index: Int): Int? {
        // if index is valid return index else returns null
        return if (index >= 0 && index < experience.flatSteps.size) index else null
    }
}
