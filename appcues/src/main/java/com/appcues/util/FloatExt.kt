@file:Suppress("unused")

package com.appcues.util

import kotlin.math.abs

private const val TOLERANCE = 0.0001f

internal infix fun Float.eq(other: Float) = abs(this - other) <= TOLERANCE

internal infix fun Float.ne(other: Float) = abs(this - other) > TOLERANCE
