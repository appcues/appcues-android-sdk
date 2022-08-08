package com.appcues.trait

interface ExperienceTrait {

    enum class ExperienceTraitLevel {
        EXPERIENCE, GROUP, STEP
    }

    /**
     * [type] is a unique key for specific trait to be registered as a valid trait in the SDK
     *
     * it is advised to follow such pattern: "@<company>/<trait-name>"
     */
    val type: String

    /**
     * [config] is a parameter most traits can have to include additional
     * information that can be used by that trait.
     */
    val config: Map<String, Any>?

    /**
     * [level] defines if this trait should be propagated to a certain point from where it was found in the data modal.
     *
     * eg: In case we find an ExperienceTrait with level STEP while reading the experience model,
     *     we will merge this trait to each group and each step
     *
     * eg: In case we find an ExperienceTrait with level GROUP while reading a parent step (group),
     *     we will leave as is, the only structure that will know this trait will be a stepGroup
     *
     */
    val level: ExperienceTraitLevel
}
