package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.remote.appcues.request.EventRequest

internal class NorCondition(private val conditions: List<Condition>) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        return conditions.none { it.evaluate(request, profileProperties) }
    }
}
