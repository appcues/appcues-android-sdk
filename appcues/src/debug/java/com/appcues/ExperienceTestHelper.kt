package com.appcues

import android.content.Context
import coil.ImageLoader
import com.appcues.di.Bootstrap
import com.appcues.di.scope.get

public fun Appcues(
    context: Context,
    accountId: String,
    applicationId: String,
    imageLoader: ImageLoader?,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues = Bootstrap
    // This creates the Koin Scope and initializes the Appcues instance within, then returns the Appcues instance
    // ready to go with the necessary dependency configuration in its scope.
    .createScope(
        context = context,
        config = AppcuesConfig(accountId, applicationId).apply {
            config?.invoke(this)
            this.imageLoader = imageLoader
        }
    ).get()
