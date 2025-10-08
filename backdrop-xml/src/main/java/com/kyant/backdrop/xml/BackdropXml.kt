package com.kyant.backdrop.xml

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import com.kyant.backdrop.xml.backdrop.*
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.presets.LiquidGlassPresets
import com.kyant.backdrop.xml.utils.LiquidGlassEffectBuilder
import com.kyant.backdrop.xml.utils.liquidGlassEffect
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import com.kyant.backdrop.xml.views.LiquidGlassView

/**
 * Main API class for the Backdrop XML library.
 * Provides convenient access to all liquid glass effects and backdrop functionality.
 * 
 * This class serves as the primary entry point for the XML version of the backdrop library,
 * offering 1:1 feature parity with the original Compose implementation.
 */
object BackdropXml {
    
    /**
     * Effects factory - provides easy access to all effect types
     */
    object Effects {
        
        /**
         * Creates a refraction effect
         */
        fun refraction(height: Float, amount: Float = height, hasDepthEffect: Boolean = false) = 
            RefractionEffect(height, amount, hasDepthEffect)
        
        /**
         * Creates a dispersion effect
         */
        fun dispersion(height: Float, amount: Float = height) = 
            DispersionEffect(height, amount)
        
        /**
         * Creates a blur effect
         */
        fun blur(radius: Float, style: com.kyant.backdrop.xml.effects.BlurEffect.BlurStyle = com.kyant.backdrop.xml.effects.BlurEffect.BlurStyle.NORMAL) = 
            BlurEffect(radius, style)
        
        /**
         * Creates a highlight effect
         */
        fun highlight(
            angle: Float, 
            falloff: Float = 2f, 
            color: Int = android.graphics.Color.WHITE, 
            alpha: Float = 0.3f
        ) = HighlightEffect(angle, falloff, color, alpha)
        
        /**
         * Creates a shadow effect
         */
        fun shadow(
            offsetX: Float, 
            offsetY: Float, 
            radius: Float, 
            color: Int = android.graphics.Color.BLACK, 
            alpha: Float = 0.25f
        ) = ShadowEffect(offsetX, offsetY, radius, color, alpha)
        
        /**
         * Creates an inner shadow effect
         */
        fun innerShadow(
            offsetX: Float, 
            offsetY: Float, 
            radius: Float, 
            color: Int = android.graphics.Color.BLACK, 
            alpha: Float = 0.2f
        ) = InnerShadowEffect(offsetX, offsetY, radius, color, alpha)
        
        /**
         * Creates a color filter effect with control parameters
         */
        fun colorFilter(brightness: Float = 0f, contrast: Float = 1f, saturation: Float = 1f, alpha: Float = 1f) =
            ColorFilterEffect(brightness, contrast, saturation, alpha)
        
        /**
         * Creates a gamma adjustment effect
         */
        fun gammaAdjustment(gamma: Float = 1.0f) = 
            GammaAdjustmentEffect(gamma)
        
        /**
         * Creates an exposure adjustment effect
         */
        fun exposureAdjustment(exposureValue: Float = 0.0f) = 
            ExposureAdjustmentEffect(exposureValue)
        
        /**
         * Creates an overexposed effect (positive exposure adjustment)
         */
        fun overexposed(exposureValue: Float = 1.0f) = 
            ExposureAdjustmentEffect(exposureValue)
        
        /**
         * Creates an underexposed effect (negative exposure adjustment)
         */
        fun underexposed(exposureValue: Float = -1.0f) = 
            ExposureAdjustmentEffect(exposureValue)
    }
    
    /**
     * Backdrop factory - provides easy access to all backdrop types
     */
    object Backdrops {
        
        /**
         * Empty backdrop that renders nothing
         */
        val empty: XmlBackdrop = EmptyXmlBackdrop
        
        /**
         * Creates a drawable-based backdrop
         */
        fun drawable(drawable: Drawable): XmlBackdrop = DrawableXmlBackdrop(drawable)
        
        /**
         * Creates a view-based backdrop
         */
        fun view(view: View): XmlBackdrop = ViewXmlBackdrop(view)
        
        /**
         * Creates a canvas-based backdrop with custom drawing
         */
        fun canvas(onDraw: (Canvas, Float, Float) -> Unit): XmlBackdrop = CanvasXmlBackdrop(onDraw)
        
        /**
         * Combines multiple backdrops into a single backdrop
         */
        fun combined(vararg backdrops: XmlBackdrop): XmlBackdrop = CombinedXmlBackdrop.of(*backdrops)
    }
    
    /**
     * Preset configurations - ready-to-use glass effects
     */
    object Presets {
        
        /**
         * iOS-style glass button
         */
        fun iosButton(cornerRadius: Float = 12f) = LiquidGlassPresets.iosGlassButton(cornerRadius)
        
        /**
         * Material Design glass card
         */
        fun materialCard(cornerRadius: Float = 16f) = LiquidGlassPresets.materialGlassCard(cornerRadius)
        
        /**
         * Frosted glass overlay
         */
        fun frostedOverlay(cornerRadius: Float = 8f) = LiquidGlassPresets.frostedGlassOverlay(cornerRadius)
        
        /**
         * Chromatic liquid glass with dispersion
         */
        fun chromaticGlass(cornerRadius: Float = 20f) = LiquidGlassPresets.chromaticLiquidGlass(cornerRadius)
        
        /**
         * Subtle glass panel
         */
        fun subtlePanel(cornerRadius: Float = 12f) = LiquidGlassPresets.subtleGlassPanel(cornerRadius)
        
        /**
         * Gaming-style liquid glass
         */
        fun gamingGlass(cornerRadius: Float = 16f) = LiquidGlassPresets.gamingLiquidGlass(cornerRadius)
        
        /**
         * Minimalist glass
         */
        fun minimalist(cornerRadius: Float = 8f) = LiquidGlassPresets.minimalistGlass(cornerRadius)
        
        /**
         * Heavy liquid glass with maximum effects
         */
        fun heavyGlass(cornerRadius: Float = 24f) = LiquidGlassPresets.heavyLiquidGlass(cornerRadius)
        
        /**
         * Notification panel glass
         */
        fun notificationPanel(cornerRadius: Float = 16f) = LiquidGlassPresets.notificationGlass(cornerRadius)
        
        /**
         * Pressed button glass
         */
        fun pressedButton(cornerRadius: Float = 12f) = LiquidGlassPresets.pressedGlassButton(cornerRadius)
        
        /**
         * Navigation bar glass
         */
        fun navigationBar() = LiquidGlassPresets.glassNavigationBar(0f)
        
        /**
         * Floating action button glass
         */
        fun fab() = LiquidGlassPresets.glassFAB(28f)
        
        /**
         * Dialog backdrop glass
         */
        fun dialogBackdrop(cornerRadius: Float = 20f) = LiquidGlassPresets.dialogGlassBackdrop(cornerRadius)
    }
    
    /**
     * Builder factory - creates effect builders for fluent configuration
     */
    fun effectBuilder(): LiquidGlassEffectBuilder = liquidGlassEffect()
    
    /**
     * Quick configuration methods for common scenarios
     */
    object QuickConfig {
        
        /**
         * Applies a preset to a LiquidGlassView
         */
        fun applyPreset(view: LiquidGlassView, preset: LiquidGlassEffectBuilder) {
            preset.applyTo(view)
        }
        
        /**
         * Applies a preset to a LiquidGlassContainer
         */
        fun applyPreset(container: LiquidGlassContainer, preset: LiquidGlassEffectBuilder) {
            preset.applyTo(container)
        }
        
        /**
         * Sets up a basic glass effect with common parameters
         */
        fun basicGlass(
            view: LiquidGlassView,
            cornerRadius: Float = 12f,
            blurRadius: Float = 8f,
            refractionHeight: Float = 6f
        ) {
            effectBuilder()
                .cornerRadius(cornerRadius)
                .blur(blurRadius)
                .subtleRefraction(refractionHeight)
                .ambientHighlight()
                .applyTo(view)
        }
        
        /**
         * Sets up an advanced glass effect with multiple features
         */
        fun advancedGlass(
            view: LiquidGlassView,
            cornerRadius: Float = 16f,
            refractionHeight: Float = 12f,
            dispersionHeight: Float = 8f,
            blurRadius: Float = 10f
        ) {
            effectBuilder()
                .cornerRadius(cornerRadius)
                .strongRefraction(refractionHeight)
                .subtleDispersion(dispersionHeight)
                .blur(blurRadius)
                .topLeftHighlight()
                .dropShadow()
                .applyTo(view)
        }
    }
}