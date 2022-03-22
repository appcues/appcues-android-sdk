package com.appcues.analytics

import com.appcues.analytics.storage.ActivityStoring
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.remote.request.ActivityRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ActivityProcessor(
    private val repository: AppcuesRepository,
    private val storage: ActivityStoring,
    private val gson: Gson,
) {
    suspend fun process(activityRequest: ActivityRequest, sync: Boolean): List<Experience> = withContext(Dispatchers.IO) {
//        val activityStorage = ActivityStorage(activityRequest, gson)
//        storage.save(activityStorage)

        // TESTING! to be removed!
//        val storedItems = storage.read()
//        val storedCount = storedItems.count()
//        Log.i("Appcues", "Activity storage count: $storedCount")
//        if (storedCount >= 10) {
//            storedItems.forEach {
//                storage.remove(it)
//            }
//        }
        // later, this will be managed internally and cleaned up during processing to network

        flush(activityRequest, sync)
    }

    // todo - this is placeholder - will evolve to actually flush the current item
    // in addition to anything in local storage pending retry
    private suspend fun flush(activity: ActivityRequest, sync: Boolean): List<Experience> {
        // this will respond with qualified experiences, if applicable
        return repository.trackActivity(activity, sync)
    }
}
