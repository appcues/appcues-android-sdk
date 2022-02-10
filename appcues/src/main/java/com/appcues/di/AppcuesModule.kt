package com.appcues.di

import com.appcues.AppcuesConfig
import com.appcues.AppcuesSession
import com.appcues.data.DefaultDataGateway
import com.appcues.domain.ShowExperienceUseCase
import com.appcues.domain.ShowUseCase
import com.appcues.domain.gateway.CustomerExperienceGateway
import com.appcues.domain.gateway.DataGateway
import com.appcues.logging.Logcues
import com.appcues.monitor.CustomerViewModel
import com.appcues.monitor.CustomerViewModelAdapter
import com.appcues.monitor.CustomerViewModelHolder
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped { AppcuesSession() }
            scoped { Logcues(config.loggingLevel) }

            scoped { CustomerViewModelAdapter(scopeId = scopeId) }
                .bind(CustomerViewModelHolder::class)
                .bind(CustomerExperienceGateway::class)

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

            viewModel {
                CustomerViewModel(
                    logcues = get(),
                    showUseCase = get(),
                )
            }
        }
    }
}
