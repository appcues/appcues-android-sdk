package com.appcues.debugger.ui.fonts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.R.string
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.debugger.ui.AppcuesSearchView
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.shared.FloatingBackButton
import com.appcues.debugger.ui.shared.copyToClipboardAndToast
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerFontList(
    viewModel: DebuggerViewModel,
    navController: NavHostController,
) {

    val filter = remember { mutableStateOf(String()) }
    val appSpecificFontsFiltered = remember {
        derivedStateOf {
            if (filter.value.isNotEmpty()) {
                viewModel.appSpecificFonts.filter { it.name.lowercase().contains(filter.value) }
            } else viewModel.appSpecificFonts
        }
    }

    val systemFontsFiltered = remember {
        derivedStateOf {
            if (filter.value.isNotEmpty()) {
                viewModel.systemFonts.filter { it.name.lowercase().contains(filter.value) }
            } else viewModel.systemFonts
        }
    }

    val allFontsFiltered = remember {
        derivedStateOf {
            if (filter.value.isNotEmpty()) {
                viewModel.allFonts.filter { it.name.lowercase().contains(filter.value) }
            } else viewModel.allFonts
        }
    }

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

        if (appSpecificFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_app_specific_title)
            fonts(appSpecificFontsFiltered.value)
        }

        if (systemFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_system_title)
            fonts(systemFontsFiltered.value)
        }

        if (allFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_all_title)
            fonts(allFontsFiltered.value)
        }
    }

    val isFirstVisible = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val isOverThreshold = with(LocalDensity.current) {
        lazyListState.firstVisibleItemScrollOffset.toDp() < firstVisibleItemOffsetThreshold
    }
    val docked = isFirstVisible.value == 0 && isOverThreshold

    FontDetailsOverlay(docked, navController, filter)
}

@Composable
private fun FontDetailsOverlay(
    docked: Boolean,
    navController: NavHostController,
    filter: MutableState<String>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 8.dp)
    ) {

        FloatingBackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp),
            docked = docked,
        ) {
            navController.popBackStack()
        }

        val elevation = animateDpAsState(if (docked) 0.dp else 12.dp, label = "search elevation")
        AppcuesSearchView(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth()
                .padding(start = 60.dp, top = 12.dp, end = 8.dp),
            height = 40.dp,
            elevation = elevation.value,
            hint = LocalContext.current.getString(string.appcues_debugger_font_details_hint),
            inputDelay = 300,
        ) { filter.value = it.lowercase() }
    }
}

private fun LazyListScope.sectionTitle(resId: Int) {
    item {
        Text(
            text = LocalContext.current.getString(resId),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalAppcuesTheme.current.brand,
        )
    }
}

private fun LazyListScope.fonts(properties: List<DebuggerFontItem>) {
    items(properties.toList()) { item ->
        ListItem(item)
    }

    item {
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun LazyItemScope.ListItem(debuggerFont: DebuggerFontItem) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .clickable { copyToClipboardAndToast(context, clipboard, debuggerFont.name) }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = debuggerFont.name,
            fontSize = 16.sp,
            fontWeight = debuggerFont.fontWeight,
            fontFamily = debuggerFont.fontFamily,
            color = LocalAppcuesTheme.current.primary
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Icon(
            painter = painterResource(R.drawable.appcues_ic_copy),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_font_details_copy_icon_description),
            modifier = Modifier
                .padding(start = 10.dp)
                .size(20.dp),
            tint = LocalAppcuesTheme.current.secondary,
        )
    }

    ListItemDivider()
}

@Composable
private fun ListItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = LocalAppcuesTheme.current.divider,
        thickness = 1.dp,
    )
}
