package com.appcues.di

import org.koin.dsl.ScopeDSL

/**
 * Implement this interface to define a module that will be loaded for each new Appcues scope
 *
 * Used at AppcuesKoinContext
 */
internal interface KoinScopePlugin {

    /**
     * Used to load new scoped factories defined in [ScopeDSL]
     */
    fun ScopeDSL.install()
}
