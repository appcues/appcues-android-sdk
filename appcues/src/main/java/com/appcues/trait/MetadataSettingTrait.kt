package com.appcues.trait

interface MetadataSettingTrait : ExperienceTrait {

    /**
     * returns a map of shared values
     */
    fun produceMetadata(): Map<String, Any?>
}
