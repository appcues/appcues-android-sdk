package com.appcues.trait

public interface PresentingTrait : ExperienceTrait {

    /**
     * Presents the experience.
     *
     * If this method cannot properly apply the trait behavior, it may throw an AppcuesTraitException,
     * ending the attempt to display the experience.
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.ModalTrait
     */
    @Throws(AppcuesTraitException::class)
    public fun present()
}
