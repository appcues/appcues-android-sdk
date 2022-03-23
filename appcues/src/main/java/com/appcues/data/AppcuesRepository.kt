package com.appcues.data

import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.model.ActivityStorage
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.appcues.util.doIfSuccess
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

internal class AppcuesRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val appcuesLocalSource: AppcuesLocalSource,
    private val experienceMapper: ExperienceMapper,
    private val gson: Gson,
) {
    private val processingActivity: HashSet<UUID> = hashSetOf()

    suspend fun getExperienceContent(experienceId: String): Experience? = withContext(Dispatchers.IO) {
        appcuesRemoteSource.getExperienceContent(experienceId).let {
            when (it) {
                is Success -> experienceMapper.map(it.value)
                is Failure -> null // Log?
            }
        }
    }

    suspend fun trackActivity(activity: ActivityRequest, sync: Boolean): List<Experience> = withContext(Dispatchers.IO) {
        val activityStorage = ActivityStorage(activity.requestId, activity.accountId, activity.userId, gson.toJson(activity))

        // mark this current item as processing before inserting into storage
        // so that any concurrent flush going on will not use it
        synchronized(this) {
            processingActivity.add(activity.requestId)
        }

        appcuesLocalSource.saveActivity(activityStorage)
        flush(activityStorage, sync)
    }

    private suspend fun flush(activity: ActivityStorage?, sync: Boolean): List<Experience> {
        val activities = appcuesLocalSource.getAllActivity()
        val itemsToFlush = mutableListOf<ActivityStorage>()

        // the block that checks which items are eligible (not already processing)
        // and sets up the next queue to process needs to be threadsafe
        synchronized(this) {
            // exclude any items that are already processing
            val available = activities.filter { !processingActivity.contains(it.requestId) }
            itemsToFlush += prepareForRetry(available)

            // append the current item, if provided - it will have been filtered out as processing
            // but if passed in as `activity` this flush is responsible for handling it
            if (activity != null) {
                itemsToFlush.add(activity)
            }

            // mark them all as requests in process
            processingActivity.addAll(itemsToFlush.map { it.requestId })
        }
        
        return post(itemsToFlush, activity, sync)
    }

    private fun prepareForRetry(available: List<ActivityStorage>): MutableList<ActivityStorage> {
        val eligible = mutableListOf<ActivityStorage>()
        // todo - logic here needs to be built out in next pass
        // 1. most recent X items only based on config
        // 2. mark others as outdated and delete from storage
        // 3. optionally handle a max age from config
        eligible += available.sortedBy { it.created }
        return eligible
    }

    private suspend fun post(queue: MutableList<ActivityStorage>, current: ActivityStorage?, sync: Boolean): List<Experience> {
        // pop the next activity off the current queue to process
        // the list is processed in chronological order
        val activity = queue.removeFirstOrNull() ?: return listOf()

        // `current` is the activity that triggered this processing, and may be qualifying
        val isCurrent = activity == current
        // only apply the `sync=1` param if we are processing the current activity (not a cache item) and it was
        // requested to be synchronous
        val syncRequest = if (isCurrent) sync else false

        val activityResult = appcuesRemoteSource
            .postActivity(activity.userId, activity.data, syncRequest)

        // todo - error handling on this network request
        val experiences = mutableListOf<Experience>()
        activityResult.doIfSuccess { response ->
            experiences += response.experiences.mapNotNull {
                // this is likely redundant, since a non-sync request wont
                // return any experiences qualified - but we want to make sure
                // we don't do any needless mapping work on something that will
                // be ignored anyway (cache items)
                if (syncRequest) experienceMapper.map(it) else null
            }
        }

        // it should only be removed from local storage on success
        appcuesLocalSource.removeActivity(activity)

        synchronized(this) {
            // always mark done processing after an attempt
            processingActivity.remove(activity.requestId)
        }

        // recurse
        return experiences + post(queue, current, sync)
    }
}
