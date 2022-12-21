package com.appcues.util

import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CompletableDeferred

internal class LanguageTranslator(
    private val contextResources: ContextResources
) {
    suspend fun translate(experience: Experience): Experience {
        // if a localized experience is requested, we'll transform it here
        var translator: Translator? = null
        var autoDetect = false
        var targetLanguageTag: String? = null

        experience.localizationTrait?.let {
            if (it.translate && !it.onDemand) {
                targetLanguageTag = it.targetLanguageTag
                if (it.sourceLanguageTag == null) {
                    autoDetect = true
                } else {
                    initialize(it.sourceLanguageTag, targetLanguageTag).doIfSuccess { experienceTranslator ->
                        translator = experienceTranslator
                    }
                }
            }
        }

        val translated = experience.copy(
            stepContainers = experience.stepContainers.map { translate(it, translator, autoDetect, targetLanguageTag) }
        )
        translator?.close()
        return translated
    }

    fun translateBlock(): (suspend (String, String?, String?) -> String) {
        return { text, sourceLanguageTag, targetLanguageTag ->
            var result = text
            sourceLanguageTag?.let { sourceTag ->
                initialize(sourceTag, targetLanguageTag)
                    .doIfSuccess { translator ->
                        translator.toTargetLanguage(text)
                            .doIfSuccess { result = it }
                    }
            }
            result
        }
    }

    private suspend fun initialize(sourceLanguageTag: String, targetLanguageTag: String?): ResultOf<Translator, Exception> {
        val targetLanguage = TranslateLanguage.fromLanguageTag(targetLanguageTag ?: contextResources.getLanguage())
        val sourceLanguage = TranslateLanguage.fromLanguageTag(sourceLanguageTag)

        if (sourceLanguage == null || targetLanguage == null) {
            // todo better error handling
            return Failure(Exception("language not found!!"))
        }

        if (sourceLanguage == targetLanguage) {
            // todo better error handling
            return Failure(Exception("no translation needed"))
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        val completion = CompletableDeferred<ResultOf<Translator, Exception>>()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                completion.complete(Success(translator))
            }
            .addOnFailureListener { exception ->
                completion.complete(Failure(exception))
            }

        return completion.await()
    }

    private suspend fun translate(
        stepContainer: StepContainer,
        experienceTranslator: Translator?,
        autoDetectExperience: Boolean,
        targetLanguageTagExperience: String?,
    ): StepContainer {
        var translator = experienceTranslator
        var autoDetect = autoDetectExperience
        var targetLanguageTag = targetLanguageTagExperience
        var closeTranslator = false

        stepContainer.localizationTrait?.let {
            if (it.translate && !it.onDemand) {
                targetLanguageTag = it.targetLanguageTag
                if (it.sourceLanguageTag == null) {
                    autoDetect = true
                } else {
                    autoDetect = false
                    initialize(it.sourceLanguageTag, it.targetLanguageTag).doIfSuccess { stepContainerTranslator ->
                        translator = stepContainerTranslator
                        closeTranslator = true // we made it, we close it
                    }
                }
            } else {
                // explicitly disabling
                translator = null
            }
        }

        val translated = stepContainer.copy(steps = stepContainer.steps.map { translate(it, translator, autoDetect, targetLanguageTag) })
        if (closeTranslator) translator?.close()
        return translated
    }

    private suspend fun translate(
        step: Step,
        stepContainerTranslator: Translator?,
        autoDetectContainer: Boolean,
        targetLanguageTagContainer: String?,
    ): Step {
        var translator = stepContainerTranslator
        var autoDetect = autoDetectContainer
        var targetLanguageTag = targetLanguageTagContainer
        var closeTranslator = false

        step.localizationTrait?.let {
            if (it.translate && !it.onDemand) {
                targetLanguageTag = it.targetLanguageTag
                if (it.sourceLanguageTag == null) {
                    autoDetect = true
                } else {
                    autoDetect = false
                    initialize(it.sourceLanguageTag, it.targetLanguageTag).doIfSuccess { stepTranslator ->
                        translator = stepTranslator
                        closeTranslator = true // we made it, we close it
                    }
                }
            } else {
                // explicitly disabling
                translator = null
            }
        }

        val translated = step.copy(content = translate(step.content, translator, autoDetect, targetLanguageTag))
        if (closeTranslator) translator?.close()
        return translated
    }

    private suspend fun translate(
        content: ExperiencePrimitive,
        stepTranslator: Translator?,
        autoDetect: Boolean,
        targetLanguageTag: String?
    ): ExperiencePrimitive {
        var translator = stepTranslator
        var closeTranslator = false

        if (autoDetect) {
            // try to detect the source language from the content of this step and create a translator
            val text = gatherText(content)
            val languageId = LanguageIdentification.getClient()
            languageId.identify(text).doIfSuccess { sourceLanguageTag ->
                initialize(sourceLanguageTag, targetLanguageTag).doIfSuccess {
                    translator = it
                    closeTranslator = true
                }
            }
        }

        val translated = translate(content, translator)
        if (closeTranslator) translator?.close()
        return translated
    }

    private suspend fun translate(content: ExperiencePrimitive, translator: Translator?): ExperiencePrimitive {
        if (translator == null) return content

        with(content) {
            return when (this) {
                is BoxPrimitive -> this.copy(items = this.items.map { translate(it, translator) })
                is ButtonPrimitive -> this.copy(content = translate(this.content, translator))
                is EmbedHtmlPrimitive -> this
                is HorizontalStackPrimitive -> this.copy(items = this.items.map { translate(it, translator) })
                is ImagePrimitive -> this
                is TextPrimitive -> {
                    if (!this.localizable) return this
                    // todo handle translation failure
                    var text = this.text
                    translator.toTargetLanguage(this.text).doIfSuccess { text = it }
                    this.copy(text = text)
                }
                is VerticalStackPrimitive -> this.copy(items = this.items.map { translate(it, translator) })
                is TextInputPrimitive -> this.copy(
                    label = translate(this.label, translator) as TextPrimitive,
                    errorLabel = translateOptional(this.errorLabel, translator) as TextPrimitive,
                    placeholder = translateOptional(this.placeholder, translator) as TextPrimitive,
                )
                is OptionSelectPrimitive -> this.copy(
                    label = translate(this.label, translator) as TextPrimitive,
                    errorLabel = translateOptional(this.errorLabel, translator) as TextPrimitive,
                    options = this.options.map { option ->
                        option.copy(
                            content = translate(option.content, translator),
                            selectedContent = translateOptional(option.selectedContent, translator),
                        )
                    }
                )
                is SpacerPrimitive -> this
            }
        }
    }

    private suspend fun translateOptional(content: ExperiencePrimitive?, translator: Translator?) : ExperiencePrimitive? {
        if (content == null) return null
        return translate(content, translator)
    }

    private suspend fun gatherText(content: ExperiencePrimitive?): String {
        if (content == null) return ""

        with(content) {
            return when (this) {
                is BoxPrimitive -> this.items.map { gatherText(it) }.joinToString()
                is ButtonPrimitive -> gatherText(this.content)
                is EmbedHtmlPrimitive -> ""
                is HorizontalStackPrimitive -> this.items.map { gatherText(it) }.joinToString()
                is ImagePrimitive -> ""
                is TextPrimitive -> this.text
                is VerticalStackPrimitive -> this.items.map { gatherText(it) }.joinToString()
                is TextInputPrimitive -> listOfNotNull(
                    this.label.text,
                    this.errorLabel?.text,
                    gatherText(this.placeholder),
                ).joinToString()
                is OptionSelectPrimitive -> listOfNotNull(
                    this.label.text,
                    this.errorLabel?.text,
                    this.options.map {
                        listOfNotNull(gatherText(it.content), gatherText(it.selectedContent)).joinToString()
                    }.joinToString(),
                ).joinToString()
                is SpacerPrimitive -> ""
            }
        }
    }
}

private suspend fun Translator.toTargetLanguage(text: String): ResultOf<String, Exception> {
    val completion = CompletableDeferred<ResultOf<String, Exception>>()

    translate(text)
        .addOnSuccessListener { translatedText ->
            completion.complete(Success(translatedText))
        }
        .addOnFailureListener { exception ->
            completion.complete(Failure(exception))
        }

    return completion.await()
}

private suspend fun LanguageIdentifier.identify(text: String): ResultOf<String, Exception> {
    val completion = CompletableDeferred<ResultOf<String, Exception>>()

    identifyLanguage(text)
        .addOnSuccessListener { languageCode ->
            completion.complete(Success(languageCode))
        }
        .addOnFailureListener { exception ->
            completion.complete(Failure(exception))
        }

    return completion.await()
}
