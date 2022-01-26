package com.appcues.domain.entity.trait

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Trait(
    val type: String,
) : Parcelable
