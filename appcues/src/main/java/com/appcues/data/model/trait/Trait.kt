package com.appcues.data.model.trait

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Trait(
    val type: String,
) : Parcelable
