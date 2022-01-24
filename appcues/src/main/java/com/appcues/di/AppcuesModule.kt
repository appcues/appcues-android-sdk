package com.appcues.di

import com.appcues.AppcuesConfig
import com.appcues.data.remote.MockExperienceRemoteGateway
import com.appcues.domain.ShowExperienceUseCase
import com.appcues.domain.ShowUseCase
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.domain.gateway.ExperienceRemoteGateway
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.monitor.CustomerActivityMonitor
import com.appcues.monitor.CustomerViewModel
import com.appcues.ui.AppcuesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped { config }
            scoped { Logcues(config.loggingLevel) }

            scoped { CustomerActivityMonitor(scopeId = scopeId) }
                .bind(ActivityMonitor::class)
                .bind(CustomerViewGateway::class)

            scoped<ExperienceRemoteGateway> { MockExperienceRemoteGateway() }

            factory {
                ShowUseCase(
                    experienceRemote = get(),
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

            viewModel { parametersHolder ->
                AppcuesViewModel(
                    experience = parametersHolder.get(),
                )
            }
        }
    }
}
