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
import com.appcues.di.KoinModule
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object DataMapperModule : KoinModule {

    override fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped {
                ExperienceMapper(
                    stepMapper = get(),
                )
            }

            scoped {
                StepMapper(
                    stepContentMapper = get(),
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
        }
    }
}
