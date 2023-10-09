package com.appcues.qualifications

import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL

internal object QualificationsModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { Qualifications(get(), get(), get()) }
    }
}
