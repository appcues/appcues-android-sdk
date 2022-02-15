package com.appcues.data.mapper.action

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.domain.entity.action.Action
import java.util.UUID

internal class ActionMapMapper(
    private val actionMapper: ActionMapper = ActionMapper(),
) {

    fun map(actions: HashMap<UUID, List<ActionResponse>>?, primitiveId: UUID): List<Action> {
        return actions?.get(primitiveId)?.map { actionMapper.map(it) } ?: arrayListOf()
    }
}
