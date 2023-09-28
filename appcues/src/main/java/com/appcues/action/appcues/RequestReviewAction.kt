package com.appcues.action.appcues

import android.content.Context
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.di.scope.AppcuesScope
import com.appcues.ui.InAppReviewActivity
import kotlinx.coroutines.CompletableDeferred

internal class RequestReviewAction(
    override val config: AppcuesConfigMap,
    private val context: Context,
    private val scope: AppcuesScope,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/request-review"
    }

    override suspend fun execute() {

        val completion = CompletableDeferred<Boolean>()
        InAppReviewActivity.completion = completion

        context.startActivity(InAppReviewActivity.getIntent(context, scope))

        completion.await()
    }
}
