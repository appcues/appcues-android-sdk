package com.appcues.data.model.styling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ComponentSize(
    val width: Int,
    val height: Int,
) : Parcelable
