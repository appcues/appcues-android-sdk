package com.appcues.debugger

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import com.appcues.debugger.model.DebuggerFontItem

internal class DebuggerFontManager(
    private val context: Context,
) {

    private val weights = mapOf(
        FontWeight.Thin to "Thin",
        FontWeight.ExtraLight to "ExtraLight",
        FontWeight.Light to "Light",
        FontWeight.Normal to "Normal",
        FontWeight.Medium to "Medium",
        FontWeight.SemiBold to "SemiBold",
        FontWeight.Bold to "Bold",
        FontWeight.ExtraBold to "ExtraBold",
        FontWeight.Black to "Black",
    )

    fun getAppSpecificFonts(): List<DebuggerFontItem> {

        val debugFonts = mutableListOf<DebuggerFontItem>()

        // for API 26+ we can attempt to read any fonts from app Resources using
        // reflection on the app.package.name.R$font class
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            try {
                val kClass = Class.forName(context.packageName + ".R\$font")
                val fontFields = kClass.fields
                for (field in fontFields) {
                    val fontResourceId = field.getInt(null)
                    val fontName = field.name
                    val typeface = context.resources.getFont(fontResourceId)
                    val fontFamily = FontFamily(Typeface(typeface))
                    debugFonts.add(DebuggerFontItem(fontName, fontFamily, FontWeight.Normal))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // also look for any fonts included by the app in /assets/fonts
        try {
            val fontsInAssets = context.assets.list("fonts")
            if (fontsInAssets != null) {
                for (assetItem in fontsInAssets) {
                    if (assetItem.endsWith(".ttf")) {
                        val name = assetItem.subSequence(0, assetItem.lastIndexOf(".ttf")).toString()
                        val typeface: android.graphics.Typeface =
                            android.graphics.Typeface.createFromAsset(context.assets, "fonts/$assetItem")
                        val fontFamily = FontFamily(Typeface(typeface))
                        debugFonts.add(DebuggerFontItem(name, fontFamily, FontWeight.Normal))
                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }

        return debugFonts.sortedBy { it.name }
    }

    fun getSystemFonts(): List<DebuggerFontItem> {
        val debugFonts = mutableListOf<DebuggerFontItem>()

        fun addSystemFont(name: String, family: FontFamily) {
            weights.forEach { (weight, weightName) ->
                debugFonts.add(DebuggerFontItem("System $name $weightName", family, weight))
            }
        }

        addSystemFont("Default", FontFamily.Default)
        addSystemFont("Serif", FontFamily.Serif)
        addSystemFont("SansSerif", FontFamily.SansSerif)
        addSystemFont("Monospace", FontFamily.Monospace)
        addSystemFont("Cursive", FontFamily.Cursive)

        return debugFonts
    }


}