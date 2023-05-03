package com.appcues.trait

/**
 * A type that describes a trait of an Experience.
 */
public interface ExperienceTrait {

    /**
     * Additional information that can be used to control how the trait is applied.
     */
    public val config: Map<String, Any>?
}
