package com.appcues.trait

public interface MetadataSettingTrait : ExperienceTrait {

    /**
     * produce a map of information that can be accessed from other traits
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.StepAnimationTrait
     *
     * @return map of shared values
     */
    public fun produceMetadata(): Map<String, Any?>
}
