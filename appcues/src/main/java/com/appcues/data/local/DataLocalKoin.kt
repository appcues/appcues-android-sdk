package com.appcues.data.local

import android.content.Context
import com.appcues.AppcuesConfig
import com.appcues.data.local.room.AppcuesDatabase
import com.appcues.data.local.room.RoomAppcuesLocalSource
import com.appcues.data.local.room.RoomWrapper
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DataLocalKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped<AppcuesLocalSource> {
            RoomAppcuesLocalSource(
                db = getAppcuesDatabase(
                    context = get(),
                    config = get(),
                )
            )
        }
    }

    private fun getAppcuesDatabase(context: Context, config: AppcuesConfig): AppcuesDatabase =
        RoomWrapper(context, config).create()
}
