package com.appcues

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import java.util.Date
import java.util.UUID

internal class Storage(
    context: Context,
    config: AppcuesConfig
) {

    private enum class Constants(val rawVal: String) {
        DeviceId("appcues.deviceId"),
        PushToken("appcues.pushToken"),
        UserId("appcues.userId"),
        GroupId("appcues.groupId"),
        IsAnonymous("appcues.isAnonymous"),
        LastContentShownAt("appcues.lastContentShownAt"),
        UserSignature("appcues.userSignature"),
    }

    private val sharedPreferences: SharedPreferences = allowReads {
        // this is also considered a read from disk
        context.getSharedPreferences("com.appcues.storage.${config.applicationId}", Context.MODE_PRIVATE)
    }

    var deviceId: String
        get() = allowReads { sharedPreferences.getString(Constants.DeviceId.rawVal, null) ?: "" }
        set(value) = sharedPreferences.edit().putString(Constants.DeviceId.rawVal, value).apply()

    var pushToken: String?
        get() = allowReads { sharedPreferences.getString(Constants.PushToken.rawVal, null) }
        set(value) = sharedPreferences.edit().putString(Constants.PushToken.rawVal, value).apply()

    var userId: String
        get() = allowReads { sharedPreferences.getString(Constants.UserId.rawVal, null) ?: "" }
        set(value) = sharedPreferences.edit().putString(Constants.UserId.rawVal, value).apply()

    var groupId: String?
        get() = allowReads { sharedPreferences.getString(Constants.GroupId.rawVal, null) }
        set(value) = sharedPreferences.edit().putString(Constants.GroupId.rawVal, value).apply()

    var isAnonymous: Boolean
        get() = allowReads { sharedPreferences.getBoolean(Constants.IsAnonymous.rawVal, true) }
        set(value) = sharedPreferences.edit().putBoolean(Constants.IsAnonymous.rawVal, value).apply()

    var lastContentShownAt: Date?
        get() = allowReads {
            sharedPreferences.getLong(Constants.LastContentShownAt.rawVal, 0)
                .run { if (this > 0) Date(this) else null }
        }
        set(value) = when (value) {
            null -> sharedPreferences.edit().remove(Constants.LastContentShownAt.rawVal).apply()
            else -> sharedPreferences.edit().putLong(Constants.LastContentShownAt.rawVal, value.time).apply()
        }

    var userSignature: String?
        get() = allowReads { sharedPreferences.getString(Constants.UserSignature.rawVal, null) }
        set(value) = sharedPreferences.edit().putString(Constants.UserSignature.rawVal, value).apply()

    init {
        if (deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString()
        }
    }

    private fun <T> allowReads(block: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            return block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}
