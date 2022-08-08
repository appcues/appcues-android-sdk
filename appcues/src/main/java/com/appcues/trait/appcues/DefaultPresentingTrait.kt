package com.appcues.trait.appcues

import android.content.Context
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.trait.PresentingTrait
import com.appcues.ui.AppcuesActivity
import org.koin.core.scope.Scope

internal class DefaultPresentingTrait(
    override val config: AppcuesConfigMap,
    private val scope: Scope,
    private val context: Context,
) : PresentingTrait {

    override fun present() {
        context.startActivity(AppcuesActivity.getIntent(context, scope.id))
    }
}
