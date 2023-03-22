package com.appcues.trait.appcues

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appcues.Appcues
import com.appcues.ElementSelector
import com.appcues.ViewElement
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigObject
import com.appcues.data.model.getConfigOrDefault
import com.appcues.debugger.screencapture.toDp
import com.appcues.findMatches
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.MetadataSettingTrait

internal class TargetElementTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {
        const val TYPE = "@appcues/target-element"
    }

    private val selector: ElementSelector? = config.getConfigObject("selector")
    private val contentDistance = config.getConfigOrDefault("contentDistanceFromTarget", 0.0)
    private val preferredPosition = config.getConfig<String>("contentPreferredPosition").toPosition()

    override fun produceMetadata(): Map<String, Any?> {
        val view = viewMatchingSelector()

        val rootView = AppcuesActivityMonitor.activity?.window?.decorView?.rootView
            ?: throw AppcuesTraitException("could not find root view")

        val displayMetrics = rootView.context.resources.displayMetrics
        val density = displayMetrics.density

        // the global position in the ViewElement includes system bars, so we need to subtract those
        // insets to get coordinates relative to the root view of what we are drawing content on top of.
        val insets = ViewCompat.getRootWindowInsets(rootView)?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE

        // the selectedView position and size is already in Dp at this point, which is what we want in metadata
        val targetRectangle = TargetRectangleInfo(
            x = (view.x - insets.left.toDp(density)).toFloat(),
            y = (view.y - insets.top.toDp(density)).toFloat(),
            width = view.width.toFloat(),
            height = view.height.toFloat(),
            contentDistance = contentDistance,
            prefPosition = preferredPosition,
        )

        return hashMapOf(TARGET_RECTANGLE_METADATA to targetRectangle)
    }

    private fun viewMatchingSelector(): ViewElement {
        // a null value here means it failed JSON deserialization
        // see ElementSelectorAdapter for more details, but it basically means that there were
        // no valid selector properties for the current ElementTargeting strategy in this app
        if (selector == null) throw AppcuesTraitException("invalid selector")

        val views = Appcues.elementTargeting.findMatches(selector)

        if (views.isEmpty()) {
            throw AppcuesTraitException("no view matching selector ${selector.toMap()}")
        }

        // if only a single match of anything, use it
        if (views.count() == 1) {
            return views[0]
        }

        // weight the selector property matches by how distinct they are considered
        val weightedMatches = views.map {
            val weight = it.selector?.evaluateMatch(selector) ?: 0
            Pair(it, weight)
        }

        // find the max weight value from all matches
        val maxWeight = weightedMatches.maxBy { it.second }.second
        val maxItems = weightedMatches.filter { it.second == maxWeight }
        if (maxItems.count() == 1) {
            return maxItems[0].first
        }

        throw AppcuesTraitException("multiple non-distinct views (${views.count()}) matched selector ${selector.toMap()}")
    }
}
