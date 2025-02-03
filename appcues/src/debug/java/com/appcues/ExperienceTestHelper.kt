package com.appcues

import android.content.Context
import coil.ImageLoader
import com.appcues.di.Bootstrap
import com.appcues.di.definition.ScopedDefinition
import com.appcues.di.scope.get
import kotlinx.coroutines.CoroutineScope

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
    coroutineScope: CoroutineScope?,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues = Bootstrap.createScope(
    context = context,
    config = AppcuesConfig(accountId, applicationId)
        .apply {
            isSnapshotTesting = true
            config?.invoke(this)
        }).also { scope ->
    // optional test overrides
    imageLoader?.let { scope.define(ImageLoader::class, ScopedDefinition { imageLoader }, true) }
    coroutineScope?.let { scope.define(CoroutineScope::class, ScopedDefinition { coroutineScope }, true) }
}.get()
