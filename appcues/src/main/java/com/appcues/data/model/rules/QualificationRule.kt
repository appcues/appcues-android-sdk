package com.appcues.data.model.rules

import com.appcues.data.remote.appcues.request.EventRequest

internal data class QualificationRule(
    val conditions: Condition,
    // other props
    val updatedAt: Long,
    val frequency: RuleFrequency,
) {

    fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        return conditions.evaluate(request, profileProperties)
    }
}
