package com.appcues.data.model

internal data class Experiment(
    val id: String,
    val group: ExperimentGroup
) {
    enum class ExperimentGroup(val analyticsName: String) {
        CONTROL("control"),
        EXPOSED("exposed"),
    }
}
