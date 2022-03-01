package com.appcues.di

import com.appcues.AppcuesConfig
import org.koin.dsl.ScopeDSL

/**
 * Implement this interface to define a module that will be loaded for each new Appcues scope
 *
 * Used at AppcuesKoinContext
 */
internal interface KoinScopePlugin {

    /**
     * Return custom module.
     *
     * Remember to make everything inside this module built inside of scope block
     */
    fun installIn(koinScope: ScopeDSL, scopeId: String, config: AppcuesConfig)
}
