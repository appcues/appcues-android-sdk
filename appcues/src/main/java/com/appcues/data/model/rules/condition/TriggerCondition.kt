package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.model.rules.Operator.Equals
import com.appcues.data.remote.appcues.request.EventRequest

internal class TriggerCondition(val event: String, val conditions: Condition?) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        val conditionEvaluation = conditions?.evaluate(request, profileProperties) ?: true

        return Equals.evaluate(request.name, event) && conditionEvaluation
    }
}
