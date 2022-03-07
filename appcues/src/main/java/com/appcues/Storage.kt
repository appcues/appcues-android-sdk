package com.appcues

import android.content.Context
import android.content.SharedPreferences
import java.util.Date
import java.util.UUID

internal class Storage(
    context: Context,
    config: AppcuesConfig
) {
    private enum class Constants(val rawVal: String) {
        DeviceId("appcues.deviceId"),
        UserId("appcues.userId"),
        GroupId("appcues.groupId"),
        IsAnonymous("appcues.isAnonymous"),
        LastContentShownAt("appcues.lastContentShownAt")
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("com.appcues.storage.${config.applicationId}", Context.MODE_PRIVATE)

    var deviceId: String
        get() = sharedPreferences.getString(Constants.DeviceId.rawVal, null) ?: ""
        set(value) = sharedPreferences.edit().putString(Constants.DeviceId.rawVal, value).apply()

    var userId: String
        get() = sharedPreferences.getString(Constants.UserId.rawVal, null) ?: ""
        set(value) = sharedPreferences.edit().putString(Constants.UserId.rawVal, value).apply()

    var groupId: String?
        get() = sharedPreferences.getString(Constants.GroupId.rawVal, null)
        set(value) = sharedPreferences.edit().putString(Constants.GroupId.rawVal, value).apply()

    var isAnonymous: Boolean
        get() = sharedPreferences.getBoolean(Constants.IsAnonymous.rawVal, true)
        set(value) = sharedPreferences.edit().putBoolean(Constants.IsAnonymous.rawVal, value).apply()

    var lastContentShownAt: Date?
        get() = sharedPreferences.getLong(Constants.LastContentShownAt.rawVal, 0).run { if (this > 0) Date(this) else null }
        set(value) = when (value) {
            null -> sharedPreferences.edit().remove(Constants.LastContentShownAt.rawVal).apply()
            else -> sharedPreferences.edit().putLong(Constants.LastContentShownAt.rawVal, value.time).apply()
        }

    init {
        if (deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString()
        }
    }
}
