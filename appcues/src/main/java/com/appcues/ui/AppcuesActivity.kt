package com.appcues.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import com.appcues.databinding.AppcuesActivityLayoutBinding
import com.appcues.di.AppcuesKoinContext
import com.appcues.logging.Logcues
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.composables.CompositionTranslation
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.util.LanguageTranslator
import org.koin.core.scope.Scope

internal class AppcuesActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_SCOPE_ID = "EXTRA_SCOPE_ID"

        fun getIntent(context: Context, scopeId: String): Intent =
            Intent(context, AppcuesActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        EXTRA_SCOPE_ID to scopeId
                    )
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private val binding by lazy { AppcuesActivityLayoutBinding.inflate(layoutInflater) }

    private val scope: Scope by lazy { AppcuesKoinContext.koin.getScope(intent.getStringExtra(EXTRA_SCOPE_ID)!!) }

    private val viewModel: AppcuesViewModel by viewModels { AppcuesViewModelFactory(scope) }

    private val shakeGestureListener: ShakeGestureListener by lazy { ShakeGestureListener(context = this) }

    private val logcues: Logcues by lazy { scope.get() }

    private val languageTranslator: LanguageTranslator by lazy { scope.get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // remove enter animation from this activity
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.appcuesActivityComposeView.setContent {
            val translateComposition = remember { mutableStateOf(false) }
            AppcuesComposition(
                viewModel = viewModel,
                shakeGestureListener = shakeGestureListener,
                logcues = logcues,
                chromeClient = EmbedChromeClient(binding.appcuesCustomViewContainer),
                onCompositionDismissed = ::finish,
                translation = CompositionTranslation(
                    null,
                    null,
                    languageTranslator.translateBlock(),
                    translateComposition)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        shakeGestureListener.start()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
        shakeGestureListener.stop()
    }

    override fun onBackPressed() {
        // if we have a back pressed dispatcher enabled then we call it. its not a good practice
        // not call super onBackPressed but sometimes people do it, in that case the debugger wont
        // consume the back press properly and there is nothing we can do about it.
        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            super.onBackPressed()
        } else {
            viewModel.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        // remove exit animation from this activity
        overridePendingTransition(0, 0)

        viewModel.onFinish()
    }
}
