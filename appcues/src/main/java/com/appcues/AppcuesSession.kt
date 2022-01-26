package com.appcues

import java.util.UUID

internal data class AppcuesSession(
    val user: String = UUID.randomUUID().toString(),
    val isAnonymous: Boolean = true,
)
