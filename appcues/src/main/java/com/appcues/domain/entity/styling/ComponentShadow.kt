package com.appcues.domain.entity.styling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ComponentShadow(
    val color: ComponentColor,
    val radius: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
) : Parcelable
