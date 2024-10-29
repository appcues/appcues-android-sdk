package com.appcues.debugger.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.appcues.AppcuesCustomComponentView
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerCustomComponentItem
import com.appcues.debugger.navigation.DebuggerRoutes.CustomComponentPage
import com.appcues.debugger.navigation.navigateDebugger
import com.appcues.debugger.ui.ds.DividerItem
import com.appcues.debugger.ui.ds.FloatingBackButton
import com.appcues.debugger.ui.ds.TextHeader
import com.appcues.debugger.ui.ds.TextPrimary
import com.appcues.debugger.ui.ds.TextSecondary
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerPluginsPage(
    viewModel: DebuggerViewModel,
    navController: NavHostController,
) {

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

        sectionTitle(R.string.appcues_debugger_plugins_custom_components_title)

        customComponents(viewModel.customComponents, navController)

        info(R.string.appcues_debugger_plugins_custom_components_info)
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

private fun LazyListScope.sectionTitle(resId: Int) {
    item {
        TextHeader(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp),
            text = LocalContext.current.getString(resId)
        )
    }
}

private fun LazyListScope.customComponents(customComponents: Map<String, AppcuesCustomComponentView>, navController: NavHostController) {
    items(customComponents.toList()) { item ->
        val identify = item.first
        val customComponent = item.second
        ListItem(identify, customComponent, navController)
    }

    item {
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }
}

private fun LazyListScope.info(resId: Int) {
    item {
        TextSecondary(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp),
            text = LocalContext.current.getString(resId)
        )
    }
}

@Composable
private fun LazyItemScope.ListItem(
    identify: String,
    component: AppcuesCustomComponentView,
    navController: NavHostController,
) {
    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .clickable {
                navController.navigateDebugger(CustomComponentPage.applyExtras(DebuggerCustomComponentItem(identify, component)))
            }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextPrimary(text = identify)
        }
    }

    DividerItem()
}
