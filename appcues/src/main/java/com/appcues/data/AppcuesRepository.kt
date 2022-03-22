package com.appcues.data

import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.model.ActivityStorage
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppcuesRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val appcuesLocalSource: AppcuesLocalSource,
    private val experienceMapper: ExperienceMapper,
    private val gson: Gson,
) {

    suspend fun getContent(contentId: String): Experience = withContext(Dispatchers.IO) {
        appcuesRemoteSource.getContent(contentId).let {
            experienceMapper.map(it)
        }
    }

    suspend fun trackActivity(activity: ActivityRequest, sync: Boolean): List<Experience> = withContext(Dispatchers.IO) {
        val activityStorage = ActivityStorage(activity.requestId, activity.accountId, activity.userId, gson.toJson(activity))
        appcuesLocalSource.save(activityStorage)
        appcuesLocalSource.remove(activityStorage)
//
//        // TESTING! to be removed!
//        val storedItems = appcuesLocalSource.read()
//        val storedCount = storedItems.count()
//        Log.i("Appcues", "Activity storage count: $storedCount")
//        if (storedCount >= 10) {
//            storedItems.forEach {
//                appcuesLocalSource.remove(it)
//            }
//        }
//        // later, this will be managed internally and cleaned up during processing to network

        appcuesRemoteSource.postActivity(activity, sync).experiences.map { experienceMapper.map(it) }
    }
}
