package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.remote.appcues.request.EventRequest

internal class NotCondition(private val condition: Condition) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        return condition.evaluate(request, profileProperties).not()
    }
}
