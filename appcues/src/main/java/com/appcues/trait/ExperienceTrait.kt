package com.appcues.trait

interface ExperienceTrait {

    /**
     * [config] is a parameter most traits can have to include additional
     * information that can be used by that trait.
     */
    val config: Map<String, Any>?
}
