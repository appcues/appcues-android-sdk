package com.appcues.data.mapper

import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL

internal object DataMapperModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped {
            ExperienceMapper(
                stepMapper = get(),
                traitsMapper = get(),
                actionsMapper = get(),
                scope = get(),
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
