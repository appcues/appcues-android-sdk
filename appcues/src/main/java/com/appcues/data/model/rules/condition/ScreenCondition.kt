package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.model.rules.Operator
import com.appcues.data.remote.appcues.request.EventRequest

internal class ScreenCondition(val value: String, val operator: Operator) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        val requestScreenName = request.context["screen_title"] as? String ?: return false

        return operator.evaluate(value, requestScreenName)
    }
}
