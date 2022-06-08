package com.appcues.analytics

import kotlin.random.Random

internal class SessionRandomizer {

    companion object {

        // _sessionRandomizer is defined in the web SDK as: A random number between 1 and 100,
        // generated every time a user visits your site in a new browser window or tab.
        // It appears to be used for targeting a % of sessions as a sample.
        private const val SESSION_RANDOMIZER_LOWER_BOUND = 0
        private const val SESSION_RANDOMIZER_UPPER_BOUND = 100
    }

    fun get(): Int {
        return Random.nextInt(SESSION_RANDOMIZER_LOWER_BOUND, SESSION_RANDOMIZER_UPPER_BOUND)
    }
}
