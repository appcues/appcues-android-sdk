package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.remote.appcues.request.EventRequest

internal class OrCondition(private val conditions: List<Condition>) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        // true in case no conditions on the list since any would return false
        if (conditions.isEmpty()) return true

        return conditions.any { it.evaluate(request, profileProperties) }
    }
}
