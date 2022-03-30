package com.appcues.data

import com.appcues.AppcuesConfig
import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.model.ActivityStorage
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.RemoteError.NetworkError
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.logging.Logcues
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.appcues.util.doIfFailure
import com.appcues.util.doIfSuccess
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class AppcuesRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val appcuesLocalSource: AppcuesLocalSource,
    private val experienceMapper: ExperienceMapper,
    private val gson: Gson,
    private val config: AppcuesConfig,
    private val logcues: Logcues,
) {
    private val processingActivity: HashSet<UUID> = hashSetOf()
    private val mutex = Mutex()

    suspend fun getExperienceContent(experienceId: String): Experience? = withContext(Dispatchers.IO) {
        appcuesRemoteSource.getExperienceContent(experienceId).let {
            when (it) {
                is Success -> experienceMapper.map(it.value)
                is Failure -> {
                    logcues.info("Experience content request failed, reason: ${it.reason}")
                    null
                }
            }
        }
    }

    suspend fun trackActivity(activity: ActivityRequest): List<Experience> = withContext(Dispatchers.IO) {

        val activityStorage = ActivityStorage(activity.requestId, activity.accountId, activity.userId, gson.toJson(activity))
        val itemsToFlush = mutableListOf<ActivityStorage>()

        // need to protect thread-safety in this section where it is determining which activities in the queue each new
        // track attempt should process
        mutex.withLock {
            // mark this current item as processing before inserting into storage
            // so that any concurrent flush going on will not use it
            processingActivity.add(activity.requestId)

            // save this item to local storage so we can retry later if needed
            appcuesLocalSource.saveActivity(activityStorage)

            // exclude any items that are already processing
            val stored = appcuesLocalSource.getAllActivity().filter { !processingActivity.contains(it.requestId) }

            itemsToFlush.addAll(prepareForRetry(stored))

            // add the current item (since it was marked as processing already)
            itemsToFlush.add(activityStorage)

            // mark them all as requests in process
            processingActivity.addAll(itemsToFlush.map { it.requestId })
        }

        post(itemsToFlush, activityStorage)
    }

    private suspend fun prepareForRetry(available: List<ActivityStorage>): MutableList<ActivityStorage> {
        val activities = available.sortedBy { it.created }
        val count = activities.count()

        // only flush max X, based on config
        // since items are sorted chronologically, take the most recent from the end
        val eligible = activities
            .takeLast(config.activityStorageMaxSize)
            .toMutableList()

        val ineligible = mutableListOf<ActivityStorage>()

        // if there are more items in storage than allowed, trim off the front, up to our allowed storage size
        // and mark as ineligible, for deletion.
        if (count > config.activityStorageMaxSize) {
            ineligible.addAll(activities.take((count - config.activityStorageMaxSize)))
        }

        // optionally, if a max age is specified, filter out items that are older
        val maxAgeSeconds = config.activityStorageMaxAge
        if (maxAgeSeconds != null) {
            val now = Date()
            val tooOld = eligible.filter {
                TimeUnit.MILLISECONDS.toSeconds(now.time - it.created.time) > maxAgeSeconds
            }
            eligible.removeAll(tooOld)
            ineligible.addAll(tooOld)
        }

        ineligible.forEach {
            appcuesLocalSource.removeActivity(it)
        }

        return eligible
    }

    private suspend fun post(queue: MutableList<ActivityStorage>, current: ActivityStorage): List<Experience> {
        // pop the next activity off the current queue to process
        // the list is processed in chronological order
        val activity = queue.removeFirstOrNull() ?: return listOf()

        // `current` is the activity that triggered this processing, and may be qualifying
        val isCurrent = activity == current

        var successful = true
        val experiences = mutableListOf<Experience>()

        if (isCurrent) {
            val qualifyResult = appcuesRemoteSource.qualify(activity.userId, activity.data)

            qualifyResult.doIfSuccess { response ->
                experiences += response.experiences.map { experienceMapper.map(it) }
            }

            qualifyResult.doIfFailure {
                logcues.info("qualify request failed, reason: $it")
                when (it) {
                    is NetworkError -> successful = false
                    else -> Unit
                }
            }
        } else {
            val activityResult = appcuesRemoteSource.postActivity(activity.userId, activity.data)

            activityResult.doIfFailure {
                logcues.info("activity request failed, reason: $it")
                when (it) {
                    is NetworkError -> successful = false
                    else -> Unit
                }
            }
        }

        // it should only be removed from local storage on success
        if (successful) {
            appcuesLocalSource.removeActivity(activity)
        }

        synchronized(this) {
            // always mark done processing after an attempt
            processingActivity.remove(activity.requestId)
        }

        return if (isCurrent) {
            // processed the qualify and should return the experiences
            experiences
        } else {
            // continue to process the queue until we get to current item
            post(queue, current)
        }
    }
}
