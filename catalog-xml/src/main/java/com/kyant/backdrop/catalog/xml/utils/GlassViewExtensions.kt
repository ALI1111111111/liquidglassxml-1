package com.kyant.backdrop.catalog.xml.utils

import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import com.kyant.backdrop.xml.LiquidGlassView

/**
 * Adds a fallback semi-transparent surface for API <31 devices where blur effects are not available.
 * This provides a frosted glass appearance on older devices.
 * 
 * @param opacity Alpha value from 0-255 (default 102 = 40% opacity)
 * @param color Base color (default WHITE)
 */
fun LiquidGlassView.addFallbackSurface(opacity: Int = 102, color: Int = Color.WHITE) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        onDrawSurface = { canvas ->
            val fallbackPaint = Paint().apply {
                this.color = color
                this.alpha = opacity
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fallbackPaint)
        }
    }
}

/**
 * Configures glass view with both modern effects (API 31+) and fallback appearance (API <31).
 * 
 * Usage patterns:
 * - Buttons/Controls: opacity 51-102 (20-40%)
 * - Backgrounds: opacity 26-77 (10-30%)
 * - Dialogs/Modals: opacity 153-204 (60-80%)
 * - Bottom Tabs/Nav: opacity 77-102 (30-40%)
 */
enum class GlassFallbackStyle(val opacity: Int, val description: String) {
    TRANSPARENT(26, "Very light glass (10%) - for subtle backgrounds"),
    LIGHT(51, "Light glass (20%) - for buttons and controls"),
    MEDIUM(102, "Medium glass (40%) - standard frosted effect"),
    HEAVY(153, "Heavy glass (60%) - for overlays"),
    SOLID(204, "Solid glass (80%) - for dialogs and modals");
    
    companion object {
        /**
         * Automatically selects appropriate fallback style based on view size.
         * Larger views use lighter glass, smaller controls use more opacity.
         */
        fun forViewSize(width: Int, height: Int): GlassFallbackStyle {
            val area = width * height
            return when {
                area > 500_000 -> TRANSPARENT // Large backgrounds
                area > 100_000 -> LIGHT // Medium panels
                area > 20_000 -> MEDIUM // Cards, buttons
                else -> HEAVY // Small controls
            }
        }
    }
}

/**
 * Applies fallback styling with automatic style selection based on view size.
 */
fun LiquidGlassView.addSmartFallback() {
    post {
        val style = GlassFallbackStyle.forViewSize(width, height)
        addFallbackSurface(opacity = style.opacity)
    }
}
