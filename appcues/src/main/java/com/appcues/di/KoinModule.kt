package com.appcues.di

import com.appcues.AppcuesConfig
import org.koin.core.module.Module

/**
 * Implement this interface to define a module that will be loaded for each new Appcues scope
 *
 * Used at AppcuesKoinContext
 */
internal interface KoinModule {

    /**
     * Return custom module.
     *
     * Remember to make everything inside this module built inside of scope block
     */
    fun install(scopeId: String, config: AppcuesConfig): Module
}
