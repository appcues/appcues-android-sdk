package com.appcues.data.model.rules

internal class IntVersion(raw: String?) : Comparable<IntVersion> {

    val versionSlots: List<Int> = (raw ?: String())
        .split("-")
        .first()
        .split(".")
        .mapNotNull { it.toIntOrNull() }
        .toMutableList()

    override fun compareTo(other: IntVersion): Int {

        val minSize = minOf(versionSlots.size, other.versionSlots.size)

        for (i in 0 until minSize) {
            when {
                versionSlots[i] > other.versionSlots[i] -> return 1
                versionSlots[i] < other.versionSlots[i] -> return -1
            }
        }

        return when {
            versionSlots.size > other.versionSlots.size -> 1
            versionSlots.size < other.versionSlots.size -> -1
            else -> 0
        }
    }
}
