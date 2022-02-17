package com.appcues.monitor

import android.content.Context
import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.CustomerExperienceGateway
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CustomerExperienceGatewayImpl(
    val scopeId: String,
    val context: Context,
) : CustomerExperienceGateway {

    override suspend fun showExperience(experience: Experience) {
        withContext(Dispatchers.Main) {
            context.startActivity(AppcuesActivity.getIntent(context, scopeId, experience))
        }
    }
}
