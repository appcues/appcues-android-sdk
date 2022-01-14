package com.appcues.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("_ABGroup")
    val abGroup: Int
)
