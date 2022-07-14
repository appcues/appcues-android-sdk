package com.appcues.data.mapper

import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.step.primitives.BoxPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ButtonPrimitiveMapper
import com.appcues.data.mapper.step.primitives.EmbedPrimitiveMapper
import com.appcues.data.mapper.step.primitives.ImagePrimitiveMapper
import com.appcues.data.mapper.step.primitives.StackPrimitiveMapper
import com.appcues.data.mapper.step.primitives.TextPrimitiveMapper
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
                stepContentMapper = get(),
                traitsMapper = get(),
                actionsMapper = get(),
            )
        }

        scoped {
            StepContentMapper(
                stackMapper = get(),
                boxMapper = get(),
                textMapper = get(),
                buttonMapper = get(),
                imageMapper = get(),
                embedMapper = get(),
            )
        }

        scoped { ButtonPrimitiveMapper() }
        scoped { ImagePrimitiveMapper() }
        scoped { StackPrimitiveMapper() }
        scoped { TextPrimitiveMapper() }
        scoped { EmbedPrimitiveMapper() }
        scoped { BoxPrimitiveMapper() }

        scoped { ActionsMapper(actionRegistry = get()) }
        scoped { TraitsMapper(traitRegistry = get()) }
    }
}
