package com.appcues

import android.content.Context
import coil.ImageLoader
import com.appcues.di.Bootstrap
import com.appcues.di.scope.get

/**
 * custom instance call that allows for custom imageLoader (used by coil)
 *
 * helper for mocking image requests coming from our custom blocks
 */
public fun Appcues(
    context: Context,
    accountId: String,
    applicationId: String,
    imageLoader: ImageLoader?,
    configApiBasePath: String? = null,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues = Bootstrap.createScope(
    context = context,
    config = AppcuesConfig(accountId, applicationId).apply {
        config?.invoke(this)
        this.imageLoader = imageLoader
        this.configApiBasePath = configApiBasePath
    }
).get()
