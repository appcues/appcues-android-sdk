package com.appcues.statemachine

import com.appcues.data.model.Experience
import java.util.UUID

internal sealed class StepReference {

    abstract fun getIndex(experience: Experience, currentStepIndex: Int): Int?

    data class StepId(val id: UUID) : StepReference() {

        override fun getIndex(experience: Experience, currentStepIndex: Int): Int? {
            return experience.flatSteps.indexOfFirst { step -> step.id == id }.let {
                // if is less then 0 it means that there was no match
                if (it < 0) null else it
            }
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
