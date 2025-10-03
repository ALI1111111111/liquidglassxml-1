/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyant.backdrop.xml.utils

import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassView
import com.kyant.backdrop.xml.views.LiquidGlassContainer

/**
 * Builder class for easily configuring liquid glass effects.
 * Provides a fluent API similar to the Compose version.
 */
class LiquidGlassEffectBuilder {
    
    private var refractionEffect: RefractionEffect? = null
    private var dispersionEffect: DispersionEffect? = null
    private var blurEffect: BlurEffect? = null
    private var highlightEffect: HighlightEffect? = null
    private var shadowEffect: ShadowEffect? = null
    private var innerShadowEffect: InnerShadowEffect? = null
    private var colorFilterEffect: ColorFilterEffect? = null
    private var gammaAdjustmentEffect: GammaAdjustmentEffect? = null
    private var exposureAdjustmentEffect: ExposureAdjustmentEffect? = null
    private var cornerRadius: Float = 0f
    private var cornerRadii: FloatArray? = null
    
    /**
     * Adds refraction effect with the specified height and amount
     */
    fun refraction(height: Float, amount: Float = height, hasDepthEffect: Boolean = false): LiquidGlassEffectBuilder {
        refractionEffect = RefractionEffect(height, amount, hasDepthEffect)
        return this
    }
    
    /**
     * Adds a subtle refraction effect
     */
    fun subtleRefraction(height: Float = 8f): LiquidGlassEffectBuilder {
        refractionEffect = RefractionEffect.subtle(height)
        return this
    }
    
    /**
     * Adds a strong refraction effect
     */
    fun strongRefraction(height: Float = 16f): LiquidGlassEffectBuilder {
        refractionEffect = RefractionEffect.strong(height)
        return this
    }
    
    /**
     * Adds dispersion effect with the specified height and amount
     */
    fun dispersion(height: Float, amount: Float = height): LiquidGlassEffectBuilder {
        dispersionEffect = DispersionEffect(height, amount)
        return this
    }
    
    /**
     * Adds a subtle dispersion effect
     */
    fun subtleDispersion(height: Float = 6f): LiquidGlassEffectBuilder {
        dispersionEffect = DispersionEffect.subtle(height)
        return this
    }
    
    /**
     * Adds a rainbow dispersion effect
     */
    fun rainbowDispersion(height: Float = 12f): LiquidGlassEffectBuilder {
        dispersionEffect = DispersionEffect.rainbow(height)
        return this
    }
    
    /**
     * Adds blur effect with the specified radius
     */
    fun blur(radius: Float, style: BlurEffect.BlurStyle = BlurEffect.BlurStyle.NORMAL): LiquidGlassEffectBuilder {
        blurEffect = BlurEffect(radius, style)
        return this
    }
    
    /**
     * Adds a subtle blur effect
     */
    fun subtleBlur(radius: Float = 4f): LiquidGlassEffectBuilder {
        blurEffect = BlurEffect.subtle(radius)
        return this
    }
    
    /**
     * Adds a strong blur effect
     */
    fun strongBlur(radius: Float = 12f): LiquidGlassEffectBuilder {
        blurEffect = BlurEffect.strong(radius)
        return this
    }
    
    /**
     * Adds highlight effect
     */
    fun highlight(angle: Float, falloff: Float = 2f, color: Int = android.graphics.Color.WHITE, alpha: Float = 0.3f): LiquidGlassEffectBuilder {
        highlightEffect = HighlightEffect(angle, falloff, color, alpha)
        return this
    }
    
    /**
     * Adds a top-left highlight
     */
    fun topLeftHighlight(falloff: Float = 2f): LiquidGlassEffectBuilder {
        highlightEffect = HighlightEffect.topLeft(falloff)
        return this
    }
    
    /**
     * Adds an ambient highlight
     */
    fun ambientHighlight(alpha: Float = 0.15f): LiquidGlassEffectBuilder {
        highlightEffect = HighlightEffect.ambient(alpha)
        return this
    }
    
    /**
     * Adds shadow effect
     */
    fun shadow(offsetX: Float, offsetY: Float, radius: Float, color: Int = android.graphics.Color.BLACK, alpha: Float = 0.25f): LiquidGlassEffectBuilder {
        shadowEffect = ShadowEffect(offsetX, offsetY, radius, color, alpha)
        return this
    }
    
    /**
     * Adds a drop shadow
     */
    fun dropShadow(offsetY: Float = 4f, radius: Float = 8f): LiquidGlassEffectBuilder {
        shadowEffect = ShadowEffect.drop(offsetY, radius)
        return this
    }
    
    /**
     * Adds an elevated shadow
     */
    fun elevatedShadow(offsetY: Float = 8f, radius: Float = 16f): LiquidGlassEffectBuilder {
        shadowEffect = ShadowEffect.elevated(offsetY, radius)
        return this
    }
    
    /**
     * Adds inner shadow effect
     */
    fun innerShadow(offsetX: Float, offsetY: Float, radius: Float, color: Int = android.graphics.Color.BLACK, alpha: Float = 0.2f): LiquidGlassEffectBuilder {
        innerShadowEffect = InnerShadowEffect(offsetX, offsetY, radius, color, alpha)
        return this
    }
    
    /**
     * Adds an inset inner shadow
     */
    fun insetShadow(offsetY: Float = 2f, radius: Float = 4f): LiquidGlassEffectBuilder {
        innerShadowEffect = InnerShadowEffect.inset(offsetY, radius)
        return this
    }
    
    /**
     * Adds a carved inner shadow
     */
    fun carvedShadow(offsetY: Float = 4f, radius: Float = 6f): LiquidGlassEffectBuilder {
        innerShadowEffect = InnerShadowEffect.carved(offsetY, radius)
        return this
    }
    
    /**
     * Adds color filter effect
     */
    fun colorFilter(brightness: Float = 0f, contrast: Float = 1f, saturation: Float = 1f, alpha: Float = 1f): LiquidGlassEffectBuilder {
        colorFilterEffect = ColorFilterEffect(brightness, contrast, saturation, alpha)
        return this
    }
    
    /**
     * Adds a vibrant color filter
     */
    fun vibrantColors(): LiquidGlassEffectBuilder {
        colorFilterEffect = ColorFilterEffect.vibrant()
        return this
    }
    
    /**
     * Adds a desaturated color filter
     */
    fun desaturatedColors(): LiquidGlassEffectBuilder {
        colorFilterEffect = ColorFilterEffect.desaturated()
        return this
    }
    
    /**
     * Adds gamma adjustment effect
     */
    fun gammaAdjustment(power: Float): LiquidGlassEffectBuilder {
        gammaAdjustmentEffect = GammaAdjustmentEffect(power)
        return this
    }
    
    /**
     * Adds a brighter gamma adjustment
     */
    fun brighterGamma(): LiquidGlassEffectBuilder {
        gammaAdjustmentEffect = GammaAdjustmentEffect.brighter()
        return this
    }
    
    /**
     * Adds a darker gamma adjustment
     */
    fun darkerGamma(): LiquidGlassEffectBuilder {
        gammaAdjustmentEffect = GammaAdjustmentEffect.darker()
        return this
    }
    
    /**
     * Adds exposure adjustment effect
     */
    fun exposureAdjustment(ev: Float): LiquidGlassEffectBuilder {
        exposureAdjustmentEffect = ExposureAdjustmentEffect(ev)
        return this
    }
    
    /**
     * Adds an overexposed (brighter) effect
     */
    fun overexposed(ev: Float = 1f): LiquidGlassEffectBuilder {
        exposureAdjustmentEffect = ExposureAdjustmentEffect.overexposed(ev)
        return this
    }
    
    /**
     * Adds an underexposed (darker) effect
     */
    fun underexposed(ev: Float = -1f): LiquidGlassEffectBuilder {
        exposureAdjustmentEffect = ExposureAdjustmentEffect.underexposed(ev)
        return this
    }
    
    /**
     * Sets uniform corner radius
     */
    fun cornerRadius(radius: Float): LiquidGlassEffectBuilder {
        cornerRadius = radius
        cornerRadii = null
        return this
    }
    
    /**
     * Sets individual corner radii
     */
    fun cornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): LiquidGlassEffectBuilder {
        cornerRadii = floatArrayOf(topLeft, topRight, bottomRight, bottomLeft)
        cornerRadius = 0f
        return this
    }
    
    /**
     * Applies all configured effects to a LiquidGlassView
     */
    fun applyTo(view: LiquidGlassView) {
        // Apply corner radius
        if (cornerRadii != null) {
            val radii = cornerRadii!!
            view.setCornerRadii(radii[0], radii[1], radii[2], radii[3])
        } else if (cornerRadius > 0f) {
            view.setCornerRadius(cornerRadius)
        }
        
        // Apply effects
        view.setRefractionEffect(refractionEffect)
        view.setDispersionEffect(dispersionEffect)
        view.setBlurEffect(blurEffect)
        view.setHighlightEffect(highlightEffect)
        view.setShadowEffect(shadowEffect)
        view.setInnerShadowEffect(innerShadowEffect)
        view.setColorFilterEffect(colorFilterEffect)
        view.setGammaAdjustmentEffect(gammaAdjustmentEffect)
        view.setExposureAdjustmentEffect(exposureAdjustmentEffect)
    }
    
    /**
     * Applies all configured effects to a LiquidGlassContainer
     */
    fun applyTo(container: LiquidGlassContainer) {
        // Apply corner radius
        if (cornerRadii != null) {
            val radii = cornerRadii!!
            container.setCornerRadii(radii[0], radii[1], radii[2], radii[3])
        } else if (cornerRadius > 0f) {
            container.setCornerRadius(cornerRadius)
        }
        
        // Apply effects
        container.setRefractionEffect(refractionEffect)
        container.setDispersionEffect(dispersionEffect)
        container.setBlurEffect(blurEffect)
        container.setHighlightEffect(highlightEffect)
        container.setShadowEffect(shadowEffect)
        container.setInnerShadowEffect(innerShadowEffect)
        container.setColorFilterEffect(colorFilterEffect)
        container.setGammaAdjustmentEffect(gammaAdjustmentEffect)
        container.setExposureAdjustmentEffect(exposureAdjustmentEffect)
    }
}

/**
 * Creates a new LiquidGlassEffectBuilder
 */
fun liquidGlassEffect(): LiquidGlassEffectBuilder {
    return LiquidGlassEffectBuilder()
}

/**
 * Extension function to apply liquid glass effects to a view
 */
fun LiquidGlassView.applyEffect(block: LiquidGlassEffectBuilder.() -> Unit) {
    liquidGlassEffect().apply(block).applyTo(this)
}

/**
 * Extension function to apply liquid glass effects to a container
 */
fun LiquidGlassContainer.applyEffect(block: LiquidGlassEffectBuilder.() -> Unit) {
    liquidGlassEffect().apply(block).applyTo(this)
}