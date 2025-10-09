package com.kyant.backdrop.xml.presets

import android.graphics.Color
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.utils.LiquidGlassEffectBuilder
import com.kyant.backdrop.xml.utils.liquidGlassEffect

/**
 * Predefined liquid glass effect presets that match common design patterns.
 * These presets provide ready-to-use configurations for typical UI scenarios.
 */
object LiquidGlassPresets {
    
    /**
     * iOS-style glass button effect
     * Features subtle refraction, ambient highlight, and soft shadow
     */
    fun iosGlassButton(cornerRadius: Float = 12f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleRefraction(8f)
            .ambientHighlight(0.2f)
            .dropShadow(2f, 6f)
            .insetShadow(1f, 3f)
    }
    
    /**
     * Material Design 3 glass card effect
     * Features blur, subtle refraction, and elevation shadow
     */
    fun materialGlassCard(cornerRadius: Float = 16f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleBlur(8f)
            .subtleRefraction(6f)
            .elevatedShadow(4f, 12f)
            .topLeftHighlight(1.5f)
    }
    
    /**
     * Frosted glass overlay effect
     * Features strong blur with subtle highlights
     */
    fun frostedGlassOverlay(cornerRadius: Float = 8f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongBlur(16f)
            .ambientHighlight(0.1f)
            .insetShadow(0f, 2f)
    }
    
    /**
     * Liquid glass with chromatic dispersion
     * Features rainbow-like color separation effects
     */
    fun chromaticLiquidGlass(cornerRadius: Float = 20f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongRefraction(12f)
            .rainbowDispersion(10f)
            .topLeftHighlight(3f)
            .dropShadow(4f, 8f)
    }
    
    /**
     * Subtle glass panel for backgrounds
     * Minimal effects for readability
     */
    fun subtleGlassPanel(cornerRadius: Float = 12f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleBlur(4f)
            .strongRefraction(12f)
            .subtleRefraction(4f)
            .ambientHighlight(0.05f)
            .rainbowDispersion(10f)
            .topLeftHighlight(3f)


    }
    
    /**
     * Gaming-style liquid glass with strong effects
     * Bold effects for gaming and entertainment apps
     */
    fun gamingLiquidGlass(cornerRadius: Float = 16f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongRefraction(16f)
            .rainbowDispersion(12f)
            .highlight(
                angle = (Math.PI * 1.25).toFloat(),
                falloff = 4f,
                color = Color.parseColor("#80FFFFFF"),
                alpha = 0.4f
            )
            .elevatedShadow(8f, 20f)
    }
    
    /**
     * Minimalist glass effect
     * Clean and simple for modern interfaces
     */
    fun minimalistGlass(cornerRadius: Float = 8f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleBlur(6f)
            .ambientHighlight(0.08f)
            .dropShadow(1f, 4f)
    }
    
    /**
     * Heavy liquid glass effect
     * Maximum distortion and effects
     */
    fun heavyLiquidGlass(cornerRadius: Float = 24f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongRefraction(20f)
            .rainbowDispersion(16f)
            .strongBlur(8f)
            .topLeftHighlight(2f)
            .elevatedShadow(12f, 24f)
            .carvedShadow(3f, 8f)
    }
    
    /**
     * Notification panel glass effect
     * Optimized for overlay panels and notifications
     */
    fun notificationGlass(cornerRadius: Float = 16f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongBlur(20f)
            .subtleRefraction(4f)
            .ambientHighlight(0.12f)
            .elevatedShadow(6f, 16f)
    }
    
    /**
     * Button glass effect with pressed state simulation
     * Designed to look like a pressed glass button
     */
    fun pressedGlassButton(cornerRadius: Float = 12f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleRefraction(6f)
            .carvedShadow(2f, 4f)
            .innerShadow(
                offsetX = 0f,
                offsetY = 1f,
                radius = 3f,
                color = Color.BLACK,
                alpha = 0.3f
            )
    }
    
    /**
     * Glass navigation bar effect
     * Designed for bottom navigation and tab bars
     */
    fun glassNavigationBar(cornerRadius: Float = 0f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongBlur(12f)
            .subtleRefraction(3f)
            .ambientHighlight(0.08f)
            .shadow(
                offsetX = 0f,
                offsetY = -2f,
                radius = 8f,
                color = Color.BLACK,
                alpha = 0.1f
            )
    }
    
    /**
     * Floating action button glass effect
     * Elevated circular glass button
     */
    fun glassFAB(cornerRadius: Float = 28f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .subtleRefraction(10f)
            .topLeftHighlight(2.5f)
            .elevatedShadow(8f, 16f)
            .ambientHighlight(0.15f)
    }
    
    /**
     * Dialog glass backdrop effect
     * For modal dialogs and overlays
     */
    fun dialogGlassBackdrop(cornerRadius: Float = 20f): LiquidGlassEffectBuilder {
        return liquidGlassEffect()
            .cornerRadius(cornerRadius)
            .strongBlur(24f)
            .subtleRefraction(2f)
            .ambientHighlight(0.05f)
            .elevatedShadow(16f, 32f)
    }
}