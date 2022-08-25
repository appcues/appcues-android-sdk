package com.appcues.data.mapper

import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DataMapperKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped {
            ExperienceMapper(
                stepMapper = get(),
                traitsMapper = get(),
                scope = get(),
                context = get(),
            )
        }

        scoped {
            StepMapper(
                traitsMapper = get(),
                actionsMapper = get(),
            )
        }

        scoped { ActionsMapper(actionRegistry = get()) }
        scoped { TraitsMapper(traitRegistry = get()) }
    }
}
