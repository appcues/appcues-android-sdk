package com.appcues.debugger

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import com.appcues.AppcuesConfig
import com.appcues.debugger.model.DebuggerFontItem
import com.appcues.logging.Logcues
import java.io.File
import java.io.IOException

internal class DebuggerFontManager(
    private val appcuesConfig: AppcuesConfig,
    private val context: Context,
    private val logcues: Logcues,
) {

    // NOTE: naming conventions here match iOS, rather than Android system defaults
    // for ease of use in the Appcues builder across platforms
    private val weights = mapOf(
        FontWeight.Thin to "Ultralight",
        FontWeight.ExtraLight to "Thin",
        FontWeight.Light to "Light",
        FontWeight.Normal to "Regular",
        FontWeight.Medium to "Medium",
        FontWeight.SemiBold to "Semibold",
        FontWeight.Bold to "Bold",
        FontWeight.ExtraBold to "Heavy",
        FontWeight.Black to "Black",
    )

    fun getAppSpecificFonts(): List<DebuggerFontItem> {
        val debugFonts = mutableListOf<DebuggerFontItem>()

        // merging default packageName with custom list
        val packageNames = mutableSetOf<String>().apply {
            add(context.packageName)
            addAll(appcuesConfig.packageNames)
        }

        packageNames.forEach { addFontResources(it, debugFonts) }

        addFontAssets(debugFonts)
        debugFonts.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return debugFonts
    }

    private fun addFontResources(packageName: String, debugFonts: MutableList<DebuggerFontItem>) {
        // for API 26+ we can attempt to read any fonts from app Resources using
        // reflection on the app.package.name.R$font class
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            try {
                val kClass = Class.forName("$packageName.R\$font")
                val fontFields = kClass.fields
                for (field in fontFields) {
                    val fontResourceId = field.getInt(null)
                    val fontName = field.name
                    val typeface = context.resources.getFont(fontResourceId)
                    val fontFamily = FontFamily(Typeface(typeface))
                    debugFonts.add(DebuggerFontItem(fontName, fontFamily, FontWeight.Normal))
                }
            } catch (ex: ClassNotFoundException) {
                logcues.error(ex)
            } catch (ex: NotFoundException) {
                logcues.error(ex)
            } catch (ex: IllegalAccessException) {
                logcues.error(ex)
            } catch (ex: IllegalArgumentException) {
                logcues.error(ex)
            }
        }
    }

    private fun addFontAssets(debugFonts: MutableList<DebuggerFontItem>) {
        // also look for any fonts included by the app in /assets/fonts
        try {
            context.assets.list("fonts")
                ?.filter {
                    it.endsWith(".ttf")
                }?.forEach {
                    val name = it.subSequence(0, it.lastIndexOf(".ttf")).toString()
                    val typeface: android.graphics.Typeface =
                        android.graphics.Typeface.createFromAsset(context.assets, "fonts/$it")
                    val fontFamily = FontFamily(Typeface(typeface))
                    debugFonts.add(DebuggerFontItem(name, fontFamily, FontWeight.Normal))
                }
        } catch (ex: IOException) {
            logcues.error(ex)
        }
    }

    fun getSystemFonts(): List<DebuggerFontItem> {
        val debugFonts = mutableListOf<DebuggerFontItem>()

        fun addSystemFont(name: String, family: FontFamily) {
            weights.forEach { (weight, weightName) ->
                debugFonts.add(DebuggerFontItem("System $name $weightName", family, weight))
            }
        }

        addSystemFont("Default", FontFamily.Default)
        addSystemFont("Monospaced", FontFamily.Monospace)
        addSystemFont("Serif", FontFamily.Serif)
        // NOTE: using Monospaced to match iOS conventions for cross platform builder ease
        // also excluding SansSerif (same as Default) and Cursive.
        // Those options can still be found and used under All Fonts in Debugger if required

        return debugFonts
    }

    fun getAllFonts(): List<DebuggerFontItem> {
        val debugFonts = mutableListOf<DebuggerFontItem>()

        File("/system/fonts/").listFiles()
            ?.filter { it.name.endsWith(".ttf") }
            ?.forEach {
                val name = it.name.subSequence(0, it.name.lastIndexOf(".ttf")).toString()
                val typeface = android.graphics.Typeface.createFromFile(it)
                val fontFamily = FontFamily(typeface)
                debugFonts.add(DebuggerFontItem(name, fontFamily, FontWeight.Normal))
            }

        debugFonts.addAll(getAppSpecificFonts())
        debugFonts.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return debugFonts
    }
}
