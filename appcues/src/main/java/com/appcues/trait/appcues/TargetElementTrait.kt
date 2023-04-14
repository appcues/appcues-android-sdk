package com.appcues.trait.appcues

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appcues.Appcues
import com.appcues.ViewElement
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
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
        const val TYPE = "@appcues/target-element-beta"
    }

    private val selectorProperties = config.getConfig<Map<String, String>>("selector") ?: mapOf()
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
        val strategy = Appcues.elementTargeting

        // a null value here means that there were no valid selector properties for
        // the current ElementTargeting strategy in this app
        val selector = strategy.inflateSelectorFrom(selectorProperties)
            ?: throw AppcuesTraitException("invalid selector $selectorProperties")

        val weightedViews = strategy.findMatches(selector)
            // if the result is null (not just empty) - that means the UI layout was not available at all
            ?: throw AppcuesTraitException("could not read application layout information")

        if (weightedViews.isEmpty()) {
            throw AppcuesTraitException("no view matching selector ${selector.toMap()}")
        }

        // views contains an array of Pairs, each item being the view and its integer match value

        // if only a single match of anything, use it
        if (weightedViews.count() == 1) {
            return weightedViews[0].first
        }

        // iterating through the array, storing the highest weight value and the list of views
        // with that weight, resetting the list when we find a higher weight
        var maxWeight = -1
        var maxWeightViews = mutableListOf<ViewElement>()

        weightedViews.forEach {
            val view = it.first
            val weight = it.second
            if (weight > maxWeight) {
                // new max weight, reset list
                maxWeight = weight
                maxWeightViews = mutableListOf(view)
            } else if (weight == maxWeight) {
                // add to the list of current max weight views
                maxWeightViews.add(view)
            }
        }

        // if this has produced a single most distinct result, use it
        if (maxWeightViews.count() == 1) {
            return maxWeightViews[0]
        }

        throw AppcuesTraitException("multiple non-distinct views (${weightedViews.count()}) matched selector ${selector.toMap()}")
    }
}
