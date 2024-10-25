package com.appcues.debugger.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.appcues.debugger.model.DebuggerCustomComponentItem
import com.appcues.debugger.ui.ds.FloatingBackButton
import com.appcues.debugger.ui.ds.TextHeader
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerCustomComponentPage(item: DebuggerCustomComponentItem, navController: NavHostController) {

    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppcuesTheme.current.background)
            .lazyColumnScrollIndicator(lazyListState),
        state = lazyListState
    ) {
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }

        item {
            TextHeader(text = item.identify)
        }

        item {
            // TODO
//            AndroidView(factory = {
//                item.component.getView()
//            })
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
