package com.appcues.data.model.rules.condition

import com.appcues.data.model.rules.Condition
import com.appcues.data.model.rules.Operator
import com.appcues.data.remote.appcues.request.EventRequest

internal class AttributesCondition(val attribute: String, val value: String?, val operator: Operator) : Condition {

    override fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean {
        return operator.evaluate(request.attributes[attribute] as? String, value)
    }
}
