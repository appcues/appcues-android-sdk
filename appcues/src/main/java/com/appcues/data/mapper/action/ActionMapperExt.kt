package com.appcues.data.mapper.action

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.domain.entity.action.Action
import java.util.UUID

internal fun HashMap<UUID, List<ActionResponse>>?.mapValuesToAction(transform: (ActionResponse) -> Action) = let { actions ->
    hashMapOf<UUID, List<Action>>().apply {
        actions?.forEach { map -> this[map.key] = map.value.map { transform(it) } }
    }
}
