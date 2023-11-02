package com.appcues.trait.appcues

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appcues.Appcues
import com.appcues.ViewElement
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.findMatches
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.MetadataSettingTrait
import com.appcues.ui.utils.getParentView
import com.appcues.util.withDensity

internal class TargetElementTrait(
    override val config: AppcuesConfigMap,
) : MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/target-element"
    }

    private val selectorProperties = config.getConfig<Map<String, String>>("selector") ?: mapOf()
    private val contentDistance = config.getConfigOrDefault("contentDistanceFromTarget", 0.0)
    private val preferredPosition = config.getConfig<String>("contentPreferredPosition").toPosition()

    @Suppress("MagicNumber")
    private val retryIntervals = (config.getConfig<List<Int>>("retryIntervals") ?: listOf(300, 600, 900, 1_200)).toMutableList()

    private val retryMilliseconds: Int?
        get() = if (retryIntervals.isNotEmpty()) retryIntervals.removeFirst() else null

    override fun produceMetadata(): Map<String, Any?> {
        val viewElement = viewMatchingSelector()

        val view = AppcuesActivityMonitor.activity?.getParentView()
            ?: throw AppcuesTraitException("could not find root view")

        return with(view) {
            // the global position in the ViewElement includes system bars, so we need to subtract those
            // insets to get coordinates relative to the root view of what we are drawing content on top of.
            val insets = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.systemBars())
                ?: Insets.NONE

            val insetsDp = withDensity { insets.toDp() }

            // the selectedView position and size is already in Dp at this point, which is what we want in metadata
            val targetRectangle = TargetRectangleInfo(
                x = viewElement.x - insetsDp.left.toFloat(),
                y = viewElement.y - insetsDp.top.toFloat(),
                width = viewElement.width.toFloat(),
                height = viewElement.height.toFloat(),
                contentDistance = contentDistance,
                prefPosition = preferredPosition,
            )

            hashMapOf(TARGET_RECTANGLE_METADATA to targetRectangle)
        }
    }

    private fun viewMatchingSelector(): ViewElement {
        val strategy = Appcues.elementTargeting

        // a null value here means that there were no valid selector properties for
        // the current ElementTargeting strategy in this app
        val selector = strategy.inflateSelectorFrom(selectorProperties)
            ?: throw AppcuesTraitException("invalid selector $selectorProperties")

        // if the result is null (not just empty) - that means the UI layout was not available at all
        val weightedViews = strategy.findMatches(selector)
            ?: throw AppcuesTraitException(
                message = "could not read application layout information",
                retryMilliseconds = retryMilliseconds,
            )

        if (weightedViews.isEmpty()) {
            throw AppcuesTraitException(
                message = "no view matching selector ${selector.toMap()}",
                retryMilliseconds = retryMilliseconds,
            )
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

        throw AppcuesTraitException(
            message = "multiple non-distinct views (${weightedViews.count()}) matched selector ${selector.toMap()}",
            retryMilliseconds = retryMilliseconds,
        )
    }
}
