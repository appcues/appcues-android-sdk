package com.appcues

import com.appcues.rules.KoinScopeRule
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.koin.test.KoinTest

internal interface AppcuesScopeTest : KoinTest, KoinScopeComponent {
    val koinTestRule: KoinScopeRule

    override val scope: Scope
        get() = koinTestRule.scope
}
