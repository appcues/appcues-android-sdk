package com.appcues.trait

interface PresentingTrait : ExperienceTrait {

    /**
     * If this method cannot properly apply the trait behavior, it may throw an AppcuesTraitException,
     * ending the attempt to display the experience.
     */
    @Throws(AppcuesTraitException::class)
    fun present()
}
