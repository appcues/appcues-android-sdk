package com.appcues.debugger.ui.fonts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.R.string
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.debugger.ui.AppcuesSearchView
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.ui.theme.AppcuesColors

private val FIRST_VISIBLE_ITEM_OFFSET_THRESHOLD = 32.dp

@Composable
internal fun DebuggerFontDetails(
    appSpecificFonts: List<DebuggerFontItem>,
    systemFonts: List<DebuggerFontItem>,
    allFonts: List<DebuggerFontItem>,
    onFontTap: (DebuggerFontItem) -> Unit,
    onBackPressed: () -> Unit,
) {

    val filter = remember { mutableStateOf(String()) }
    val appSpecificFontsFiltered = derivedStateOf {
        if (filter.value.isNotEmpty()) {
            appSpecificFonts.filter { it.name.lowercase().contains(filter.value) }
        } else appSpecificFonts
    }

    val systemFontsFiltered = derivedStateOf {
        if (filter.value.isNotEmpty()) {
            systemFonts.filter { it.name.lowercase().contains(filter.value) }
        } else systemFonts
    }

    val allFontsFiltered = derivedStateOf {
        if (filter.value.isNotEmpty()) {
            allFonts.filter { it.name.lowercase().contains(filter.value) }
        } else allFonts
    }

    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppcuesColors.DebuggerBackground)
            .lazyColumnScrollIndicator(lazyListState),
        state = lazyListState
    ) {
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }

        if (appSpecificFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_app_specific_title)
            fonts(appSpecificFontsFiltered.value, onFontTap)
        }

        if (systemFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_system_title)
            fonts(systemFontsFiltered.value, onFontTap)
        }

        if (allFontsFiltered.value.any()) {
            sectionTitle(R.string.appcues_debugger_font_details_all_title)
            fonts(allFontsFiltered.value, onFontTap)
        }
    }

    val keepBackButtonDocked = lazyListState.firstVisibleItemIndex == 0 &&
        with(LocalDensity.current) { lazyListState.firstVisibleItemScrollOffset.toDp() < FIRST_VISIBLE_ITEM_OFFSET_THRESHOLD }
    val elevation = animateDpAsState(if (keepBackButtonDocked) 0.dp else 12.dp)

    FontDetailsOverlay(elevation, onBackPressed, filter)
}

@Composable
private fun FontDetailsOverlay(
    elevation: State<Dp>,
    onBackPressed: () -> Unit,
    filter: MutableState<String>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 8.dp)
    ) {

        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp),
            elevation = elevation.value
        ) { onBackPressed() }

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

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    elevation: Dp,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .size(48.dp)
            .clip(RoundedCornerShape(percent = 100))
            .clickable { onBackPressed() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation, RoundedCornerShape(percent = 100))
                .clip(RoundedCornerShape(percent = 100))
                .size(32.dp)
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.appcues_ic_back),
                contentDescription = LocalContext.current.getString(R.string.appcues_debugger_back_description)
            )
        }
    }
}

private fun LazyListScope.sectionTitle(resId: Int) {
    item {
        Text(
            text = LocalContext.current.getString(resId),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppcuesColors.HadfieldBlue,
        )
    }
}

private fun LazyListScope.fonts(properties: List<DebuggerFontItem>, onFontTap: (DebuggerFontItem) -> Unit) {
    items(properties.toList()) { item ->
        ListItem(item, onFontTap)
    }

    item {
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun LazyItemScope.ListItem(debuggerFont: DebuggerFontItem, onFontTap: (DebuggerFontItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .clickable { onFontTap(debuggerFont) }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = debuggerFont.name,
            fontSize = 16.sp,
            fontWeight = debuggerFont.fontWeight,
            fontFamily = debuggerFont.fontFamily,
            color = AppcuesColors.Infinity
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Icon(
            painter = painterResource(R.drawable.appcues_ic_copy),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_font_details_copy_icon_description),
            modifier = Modifier
                .padding(start = 10.dp)
                .size(20.dp),
            tint = AppcuesColors.SharkbaitOhAh,
        )
    }

    ListItemDivider()
}

@Composable
private fun ListItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppcuesColors.WhisperBlue,
        thickness = 1.dp,
    )
}
