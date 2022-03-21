package com.appcues.trait.appcues

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.appcues.trait.ContentHolderTrait

internal class CarouselTrait(override val config: HashMap<String, Any>?) : ContentHolderTrait {

    @Composable
    override fun BoxScope.CreateContentHolder(pages: List<@Composable () -> Unit>, pageIndex: Int) {
        // this will be changed on upcoming task
        // implement view pager with scrollable behavior for the carousel
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(Color(color = 0x405C5CFF))
        ) {
            pages[pageIndex]()
        }
    }
}
