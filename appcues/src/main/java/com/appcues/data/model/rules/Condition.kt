package com.appcues.data.model.rules

import com.appcues.data.remote.appcues.request.EventRequest

internal interface Condition {

    fun evaluate(request: EventRequest, profileProperties: Map<String, Any>?): Boolean
}
