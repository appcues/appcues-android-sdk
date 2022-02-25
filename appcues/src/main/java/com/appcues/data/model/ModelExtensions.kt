package com.appcues.data.model

typealias AppcuesConfigMap = HashMap<String, Any>?

internal inline fun <reified T : Any?> AppcuesConfigMap.getConfigOrDefault(key: String, default: T): T {
    // if hash config is null, return default
    if (this == null) return default
    // get value by key else return default
    return getConfig<T>(key) ?: default
}

internal inline fun <reified T : Any?> AppcuesConfigMap.getConfig(key: String): T? {
    // if hash config is null, return default
    if (this == null) return null
    // get value by key else return default
    return get(key)?.let {
        // if is instance of T return it else return default
        if (it is T) it else null
    }
}
