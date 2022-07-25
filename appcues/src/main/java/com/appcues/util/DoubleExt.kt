package com.appcues.util

import kotlin.math.abs

private const val TOLERANCE = 0.000001
internal infix fun Double.eq(other: Double) = abs(this - other) <= TOLERANCE
internal infix fun Double.ne(other: Double) = abs(this - other) > TOLERANCE
