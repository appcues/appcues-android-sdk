package com.appcues.ui

import android.content.Context
import android.content.Intent
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
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

internal class AppcuesActivity : AppCompatActivity(), AppcuesKoinComponent {

    companion object {

        private const val EXTRA_SCOPE_ID = "EXTRA_SCOPE_ID"
        private const val EXTRA_EXPERIENCE = "EXTRA_EXPERIENCE"

        fun getIntent(context: Context, scopeId: String, experienceId: UUID): Intent =
            Intent(context, AppcuesActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        EXTRA_SCOPE_ID to scopeId,
                        EXTRA_EXPERIENCE to experienceId
                    )
                )
            }
    }

    override val scopeId: String by lazy { intent.getSerializableExtra(EXTRA_SCOPE_ID) as String }

    private val experienceId: UUID by lazy { intent.getSerializableExtra(EXTRA_EXPERIENCE) as UUID }

    private val viewModel: AppcuesViewModel by viewModel { parametersOf(experienceId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppcuesTheme {
                CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { finishAnimated() }) {
                    val experience = remember { viewModel.experience }
                    DialogTrait {
                        experience.value?.steps?.first()?.content?.Compose()
                    }
                }
            }
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
