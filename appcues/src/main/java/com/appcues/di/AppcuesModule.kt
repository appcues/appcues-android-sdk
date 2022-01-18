package com.appcues.di

import com.appcues.AppcuesConfig
import com.appcues.data.DefaultExperienceGateway
import com.appcues.domain.ShowUseCase
import com.appcues.domain.gateway.CustomerViewGateway
import com.appcues.domain.gateway.ExperienceGateway
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import com.appcues.monitor.CustomerActivityMonitor
import com.appcues.monitor.CustomerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(config: AppcuesConfig, dependencyProvider: DependencyProvider): Module = module {
        single { config }
        single { Logcues(get<AppcuesConfig>().loggingLevel) }

        single { CustomerActivityMonitor(dependencyProvider = dependencyProvider) }
            .bind(ActivityMonitor::class)
            .bind(CustomerViewGateway::class)

        single<ExperienceGateway> { DefaultExperienceGateway() }

        factory {
            ShowUseCase(
                experienceGateway = get(),
                customerViewGateway = get()
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
