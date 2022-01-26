package com.appcues.domain.entity.action

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Action(
    val on: OnAction,
    val type: String,
) : Parcelable
