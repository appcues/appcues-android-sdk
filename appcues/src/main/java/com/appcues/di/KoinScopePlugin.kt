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
     * Used to load new scoped factories defined in [ScopeDSL]
     *
     * [config] AppcuesConfig for this session
     *
     */
    fun ScopeDSL.install(config: AppcuesConfig)
}
