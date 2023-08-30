package com.appcues.trait

/**
 * A trait that can show a container of one or more steps of an Experience.
 */
internal interface PresentingTrait : ExperienceTrait {

    /**
     * Show the container for the applicable group of steps in the Experience.
     *
     * If this method cannot properly apply the trait behavior, it may throw an AppcuesTraitException,
     * ending the attempt to display the experience.
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.ModalTrait
     */
    @Throws(AppcuesTraitException::class)
    fun present()

    /**
     * Remove the container for the applicable group of steps in the Experience.
     */
    fun remove()
}
