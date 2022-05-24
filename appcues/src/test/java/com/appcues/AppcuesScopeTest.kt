package com.appcues

import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.koin.test.KoinTest

interface AppcuesScopeTest : KoinTest, KoinScopeComponent {
    val koinTestRule: AppcuesKoinTestRule

    override val scope: Scope
        get() = koinTestRule.scope
}
