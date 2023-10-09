package com.appcues.data.local

import com.appcues.data.local.room.RoomAppcuesLocalSource
import com.appcues.data.local.room.RoomRulesLocalSource
import com.appcues.data.local.room.RoomWrapper
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL

internal object DataLocalModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { RoomWrapper(get(), get()).create() }

        scoped<AppcuesLocalSource> { RoomAppcuesLocalSource(db = get()) }

        scoped<RulesLocalSource> { RoomRulesLocalSource(db = get()) }
    }
}
