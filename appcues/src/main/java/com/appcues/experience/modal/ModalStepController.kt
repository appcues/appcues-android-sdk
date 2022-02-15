package com.appcues.experience.modal

import android.content.Intent
import com.appcues.R.anim
import com.appcues.domain.entity.Experience
import com.appcues.experience.StepController
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.intentActionFinish
import com.appcues.monitor.sendLocalBroadcast
import com.appcues.ui.AppcuesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ModalStepController(
    private val scopeId: String,
    private val experience: Experience,
) : StepController {

    override suspend fun begin() {
        withContext(Dispatchers.Main) {
            AppcuesActivityMonitor.activity?.let {
                it.startActivity(AppcuesActivity.getIntent(it, scopeId, experience.id))
                it.overridePendingTransition(anim.fade_in, anim.fade_out)
            }
        }
    }

    override suspend fun end() {
        withContext(Dispatchers.Main) {
            AppcuesActivityMonitor.activity?.let {
                it.sendLocalBroadcast(Intent(it.intentActionFinish()))
            }
        }
    }
}
