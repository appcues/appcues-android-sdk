package com.appcues.data.remote.request

import com.appcues.data.MoshiConfiguration.SerializeNull
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ActivityRequest(
    @Json(name = "request_id")
    val requestId: UUID = UUID.randomUUID(),
    val events: List<EventRequest>? = null,
    @Json(name = "profile_update")
    val profileUpdate: MutableMap<String, Any>? = null,
    @Json(name = "user_id")
    val userId: String,
    @Json(name = "account_id")
    val accountId: String,
    @Json(name = "group_id")
    @SerializeNull
    val groupId: String? = null,
    @Json(name = "group_update")
    val groupUpdate: Map<String, Any>? = null,
    @Json(ignore = true)
    val timestamp: Date = Date(),
)
