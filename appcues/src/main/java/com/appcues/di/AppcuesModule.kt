package com.appcues.di

import com.appcues.di.scope.AppcuesScopeDSL

internal interface AppcuesModule {

    fun AppcuesScopeDSL.install()
}
