package com.appcues

import com.appcues.di.component.AppcuesComponent
import com.appcues.di.scope.AppcuesScope
import com.appcues.rules.TestScopeRule

internal interface AppcuesScopeTest : AppcuesComponent {

    val scopeRule: TestScopeRule

    override val scope: AppcuesScope
        get() = scopeRule.scope
}
