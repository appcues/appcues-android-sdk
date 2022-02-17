package com.appcues.experience.container

import android.content.Context
import com.appcues.data.model.Experience
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DialogModalPresenter(
    val scopeId: String,
    val context: Context,
) {

    suspend fun show(experience: Experience) {
        withContext(Dispatchers.Main) {
            context.startActivity(AppcuesActivity.getIntent(context, scopeId, experience))
        }
    }
}
