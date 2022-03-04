package com.appcues

import java.util.UUID

internal data class AppcuesSession(
    var userId: String = UUID.randomUUID().toString(),
    var groupId: String? = null,
    var isAnonymous: Boolean = false,
)
