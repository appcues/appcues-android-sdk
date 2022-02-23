package com.appcues.di

import com.appcues.AppcuesConfig
import org.koin.core.module.Module

internal interface KoinModule {

    fun install(scopeId: String, config: AppcuesConfig): Module
}
