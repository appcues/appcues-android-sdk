package com.appcues.di

import com.appcues.AppcuesConfig
import com.appcues.AppcuesSession
import com.appcues.data.DefaultDataGateway
import com.appcues.domain.ShowExperienceUseCase
import com.appcues.domain.ShowUseCase
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.domain.gateway.DataGateway
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.monitor.CustomerActivityMonitor
import com.appcues.monitor.CustomerViewModel
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

            scoped { CustomerActivityMonitor(scopeId = scopeId) }
                .bind(ActivityMonitor::class)
                .bind(CustomerViewGateway::class)

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
                    customerView = get(),
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
