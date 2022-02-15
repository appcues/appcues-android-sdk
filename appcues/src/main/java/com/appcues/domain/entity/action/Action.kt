package com.appcues.domain.entity.action

internal data class Action(
    val on: OnAction,
    val type: String,
    val config: HashMap<String, Any>?
)
