package com.appcues.di

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesScope
import com.appcues.AppcuesSession
import com.appcues.data.DefaultDataGateway
import com.appcues.domain.ShowExperienceUseCase
import com.appcues.domain.ShowUseCase
import com.appcues.domain.gateway.CustomerExperienceGateway
import com.appcues.domain.gateway.DataGateway
import com.appcues.logging.Logcues
import com.appcues.monitor.CustomerExperienceGatewayImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped {
                Appcues(
                    logcues = getScope(scopeId).get(),
                    appcuesScope = getScope(scopeId).get(),
                )
            }

            scoped { AppcuesSession() }
            scoped { Logcues(config.loggingLevel) }

            scoped<CustomerExperienceGateway> {
                CustomerExperienceGatewayImpl(
                    scopeId = scopeId,
                    context = get()
                )
            }

            scoped {
                AppcuesScope(
                    logcues = get(),
                    showUseCase = get(),
                )
            }

            scoped<DataGateway> {
                DefaultDataGateway(
                    appcuesRemoteSource = get(),
                )
            }

            factory {
                ShowUseCase(
                    data = get(),
                    showExperienceUseCase = get(),
                )
            }

            factory {
                ShowExperienceUseCase(
                    customerExperience = get(),
                )
            }
        }
    }
}
