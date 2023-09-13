package com.appcues

import android.content.Context
import coil.ImageLoader
import com.appcues.di.AppcuesKoinContext

public fun Appcues(
    context: Context,
    accountId: String,
    applicationId: String,
    imageLoader: ImageLoader?,
    config: (AppcuesConfig.() -> Unit)? = null,
): Appcues = AppcuesKoinContext
    // This creates the Koin Scope and initializes the Appcues instance within, then returns the Appcues instance
    // ready to go with the necessary dependency configuration in its scope.
    .createAppcuesScope(
        context = context,
        config = AppcuesConfig(accountId, applicationId).apply {
            config?.invoke(this)
            this.imageLoader = imageLoader
        }
    ).get()
