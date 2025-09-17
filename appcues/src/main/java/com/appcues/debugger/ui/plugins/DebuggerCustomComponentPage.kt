package com.appcues.debugger.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.appcues.AppcuesExperienceActions
import com.appcues.debugger.model.DebuggerCustomComponentItem
import com.appcues.debugger.ui.ds.FloatingBackButton
import com.appcues.debugger.ui.ds.TextPrimaryHighLighted
import com.appcues.debugger.ui.ds.TextSecondary
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.ui.extensions.conditional
import com.appcues.util.beautify

private val firstVisibleItemOffsetThreshold = 56.dp

internal class DebuggerExperienceActions(private val log: SnapshotStateList<String>) : AppcuesExperienceActions {

    override fun triggerBlockActions() {
        log.add("Trigger block actions")
    }

    override fun nextStep() {
        log.add("go to next step")
    }

    override fun previousStep() {
        log.add("go to previous step")
    }

    override fun close(markComplete: Boolean) {
        log.add("close experience (markComplete:$markComplete)")
    }

    override fun track(name: String, properties: Map<String, Any>?) {
        if (properties != null) {
            log.add("track(name = $name, properties = $properties)")
        } else {
            log.add("track(name = $name)")
        }
    }

    override fun updateProfile(properties: Map<String, String>) {
        log.add("updateProfile(profile = $properties)")
    }
}

@Composable
internal fun DebuggerCustomComponentPage(item: DebuggerCustomComponentItem, navController: NavHostController) {

    val lazyListState = rememberLazyListState()
    val logs = remember { mutableStateListOf<String>() }
    val controller = remember { DebuggerExperienceActions(logs) }
    val theme = LocalAppcuesTheme.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppcuesTheme.current.background)
            .lazyColumnScrollIndicator(lazyListState)
            .padding(horizontal = 16.dp),
        state = lazyListState
    ) {
        item {
            Row(
                modifier = Modifier
                    .height(88.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextPrimaryHighLighted(text = item.identify)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.backgroundBranded)
                    .padding(12.dp)
            ) {
                val configMap = remember {
                    if (item.component.getDebugConfig().isNullOrEmpty()) {
                        "empty"
                    } else {
                        item.component.getDebugConfig().toString().beautify(indentationMultiplier = 4)
                    }
                }

                TextSecondary(text = "config = $configMap")
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 24.dp)
                        .border(
                            width = 1.dp,
                            brush = SolidColor(theme.brand),
                            shape = RoundedCornerShape(0.dp)
                        ),
                    factory = { item.component.getView(controller, item.component.getDebugConfig()) }
                )
            }
        }

        itemsIndexed(logs.asReversed()) { index, log ->
            Row(
                modifier = Modifier
                    .animateItem()
                    .fillParentMaxWidth()
                    .conditional(index == 0) { clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) }
                    .conditional(index == logs.size - 1) { clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)) }
                    .background(theme.backgroundBranded)
                    .padding(horizontal = 12.dp)
            ) {
                Column {
                    TextSecondary(modifier = Modifier.padding(vertical = 8.dp), text = log)
                    if (index < logs.size - 1) {
                        Divider(color = theme.secondary.copy(alpha = 0.5f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.padding(16.dp))
        }
    }

    val scrollState = rememberScrollState()
    val docked = with(LocalDensity.current) { scrollState.value.toDp() < firstVisibleItemOffsetThreshold }

    FloatingBackButton(
        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
        docked = docked
    ) {
        navController.popBackStack()
    }
}
