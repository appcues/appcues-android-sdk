package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.remote.appcues.request.EventRequest

internal class InvalidCondition : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        // should we default to true or false on invalid condition?
        return false
    }
}
