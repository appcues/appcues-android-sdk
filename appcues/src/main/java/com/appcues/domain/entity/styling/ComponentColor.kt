package com.appcues.domain.entity.styling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ComponentColor(
    val light: Long,
    val dark: Long? = null,
) : Parcelable
