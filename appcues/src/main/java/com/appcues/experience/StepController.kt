package com.appcues.experience

interface StepController {

    suspend fun begin()

    suspend fun end()

    suspend fun forwardStep() {}
    suspend fun rewindStep() {}
    suspend fun jumpToStep(position: Int) {}
}
