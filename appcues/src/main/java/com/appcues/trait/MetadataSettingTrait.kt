package com.appcues.trait

/**
 * A trait that produces metadata that may be accessed and used by other traits.
 */
internal interface MetadataSettingTrait : ExperienceTrait {

    /**
     * Produce a map of information that can be accessed from other traits.
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.StepAnimationTrait
     *
     * @return map of shared values
     */
    fun produceMetadata(): Map<String, Any?>
}
