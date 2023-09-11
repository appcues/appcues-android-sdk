package com.appcues.data.session

import android.content.Context
import android.content.SharedPreferences
import com.appcues.AppcuesConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

internal class PrefSessionLocalSource(
    private val context: Context,
    private val config: AppcuesConfig
) {

    companion object {

        private const val KEY_DEVICE_ID = "appcues.deviceId"
        private const val KEY_USER_ID = "appcues.userId"
        private const val KEY_GROUP_ID = "appcues.groupId"
        private const val KEY_IS_ANONYMOUS = "appcues.isAnonymous"
        private const val KEY_LAST_CONTENT_SHOWN_AT = "appcues.lastContentShownAt"
        private const val KEY_USER_SIGNATURE = "appcues.userSignature"
    }

    private fun getSharedPrefs(): SharedPreferences =
        context.getSharedPreferences("com.appcues.storage.${config.applicationId}", Context.MODE_PRIVATE)

    suspend fun getDeviceId(): String = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getString(KEY_DEVICE_ID, null) ?: run {
            // when device Id is not present we generate a new one (should happen only once)
            UUID.randomUUID().toString().also {
                setDeviceId(it)
            }
        }
    }

    suspend fun setDeviceId(deviceId: String) = withContext(Dispatchers.IO) {
        getSharedPrefs().edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getString(KEY_USER_ID, null)
    }

    suspend fun setUserId(userId: String) = withContext(Dispatchers.IO) {
        getSharedPrefs().edit().putString(KEY_USER_ID, userId).apply()
    }

    suspend fun getGroupId(): String? = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getString(KEY_GROUP_ID, null)
    }

    suspend fun setGroupId(groupId: String?) = withContext(Dispatchers.IO) {
        getSharedPrefs().edit().putString(KEY_GROUP_ID, groupId).apply()
    }

    suspend fun isAnonymous(): Boolean = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getBoolean(KEY_IS_ANONYMOUS, true)
    }

    suspend fun isAnonymous(isAnonymous: Boolean) = withContext(Dispatchers.IO) {
        getSharedPrefs().edit().putBoolean(KEY_IS_ANONYMOUS, isAnonymous).apply()
    }

    suspend fun getLastContentShownAt(): Date? = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getLong(KEY_LAST_CONTENT_SHOWN_AT, 0L)
            .let { if (it > 0L) Date(it) else null }
    }

    suspend fun setLastContentShownAt(lastContentShownAt: Date?) = withContext(Dispatchers.IO) {
        when (lastContentShownAt) {
            null -> getSharedPrefs().edit().remove(KEY_LAST_CONTENT_SHOWN_AT).apply()
            else -> getSharedPrefs().edit().putLong(KEY_LAST_CONTENT_SHOWN_AT, lastContentShownAt.time).apply()
        }
    }

    suspend fun getUserSignature(): String? = withContext(Dispatchers.IO) {
        return@withContext getSharedPrefs().getString(KEY_USER_SIGNATURE, null)
    }

    suspend fun setUserSignature(userSignature: String?) = withContext(Dispatchers.IO) {
        getSharedPrefs().edit().putString(KEY_USER_SIGNATURE, userSignature).apply()
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        getSharedPrefs().edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_SIGNATURE)
            .remove(KEY_IS_ANONYMOUS)
            .remove(KEY_GROUP_ID)
            .remove(KEY_LAST_CONTENT_SHOWN_AT)
            .apply()
    }
}
