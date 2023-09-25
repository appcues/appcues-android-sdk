package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.di.scope.AppcuesScope
import com.appcues.ui.InAppReviewActivity
import kotlinx.coroutines.CompletableDeferred

internal class RequestReviewAction(
    override val config: AppcuesConfigMap,
    private val scope: AppcuesScope,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/request-review"
    }

    override suspend fun execute() {

        val completion = CompletableDeferred<Boolean>()
        InAppReviewActivity.completion = completion

        scope.context.startActivity(InAppReviewActivity.getIntent(scope))

        completion.await()
    }
}
