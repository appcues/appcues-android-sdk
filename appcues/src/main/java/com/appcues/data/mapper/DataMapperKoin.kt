package com.appcues.data.mapper

import com.appcues.AppcuesConfig
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DataMapperKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped {
            ExperienceMapper(
                stepMapper = get(),
                traitsMapper = get(),
            )
        }

        scoped {
            StepMapper(
                stepContentMapper = get(),
                traitsMapper = get(),
            )
        }

        scoped {
            StepContentMapper(
                stackMapper = get(),
                textMapper = get(),
                buttonMapper = get(),
                imageMapper = get(),
            )
        }

        scoped { ButtonPrimitiveMapper(actionsMapper = get()) }
        scoped { ImagePrimitiveMapper(actionsMapper = get()) }
        scoped { StackPrimitiveMapper(actionsMapper = get()) }
        scoped { TextPrimitiveMapper(actionsMapper = get()) }

        scoped { ActionsMapper(actionRegistry = get()) }
        scoped { TraitsMapper(traitRegistry = get()) }
    }
}
