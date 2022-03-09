package com.appcues.data.remote.request

import com.google.gson.annotations.SerializedName
import java.util.UUID

internal data class ActivityRequest(
    @SerializedName("request_id")
    val requestId: UUID = UUID.randomUUID(),
    val events: List<EventRequest>? = null,
    @SerializedName("profile_update")
    val profileUpdate: HashMap<String, Any>? = null,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("account_id")
    val accountId: String,
    @SerializedName("group_id")
    val groupId: String? = null,
    @SerializedName("group_update")
    val groupUpdate: HashMap<String, Any>? = null
)
