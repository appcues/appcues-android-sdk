package com.appcues.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.bundleOf
import com.appcues.R
import com.appcues.di.AppcuesKoinComponent
import com.appcues.domain.entity.Experience
import com.appcues.monitor.intentActionFinish
import com.appcues.monitor.registerLocalReceiver
import com.appcues.monitor.unregisterLocalReceiver
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class AppcuesActivity : AppCompatActivity(), AppcuesKoinComponent {

    companion object {

        private const val EXTRA_SCOPE_ID = "EXTRA_SCOPE_ID"
        private const val EXTRA_EXPERIENCE = "EXTRA_EXPERIENCE"
        private const val EXTRA_PARENT_INTENT_ACTION_FINISH = "EXTRA_PARENT_INTENT_ACTION_FINISH"

        fun getIntent(parent: Activity, scopeId: String, experience: Experience): Intent =
            Intent(parent, AppcuesActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        EXTRA_SCOPE_ID to scopeId,
                        EXTRA_EXPERIENCE to experience,
                        EXTRA_PARENT_INTENT_ACTION_FINISH to parent.intentActionFinish(),
                    )
                )
            }
    }

    override val scopeId: String by lazy { intent.getStringExtra(EXTRA_SCOPE_ID)!! }

    private val experience: Experience by lazy { intent.getParcelableExtra(EXTRA_EXPERIENCE)!! }

    private val parentIntentActionFinish: String by lazy { intent.getStringExtra(EXTRA_PARENT_INTENT_ACTION_FINISH)!! }

    private val viewModel: AppcuesViewModel by viewModel { parametersOf(experience) }

    private val broadcastReceiver = AppcuesBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLocalReceiver(broadcastReceiver, IntentFilter(parentIntentActionFinish))
        setContent {
            AppcuesTheme {
                CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { finishAnimated() }) {
                    val experience = remember { viewModel.experienceState }
                    DialogTrait {
                        experience.value.steps.first().content.Compose()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterLocalReceiver(broadcastReceiver)
    }

    inner class AppcuesBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            finishAnimated()
        }
    }

    private fun finishAnimated() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    @Preview(
        name = "First Preview",
        showBackground = true
    )
    @Composable
    fun Preview() {
        AppcuesTheme {
            DialogTrait {
                experienceModalOne.Compose()
            }
        }
    }
}
