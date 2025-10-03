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

package com.kyant.backdrop.xml.effects

import android.graphics.BlurMaskFilter
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlin.math.pow

/**
 * Configuration for liquid glass refraction effects.
 * This creates the signature "liquid glass" distortion that bends background content.
 * 
 * @param height The depth of the refraction effect in pixels
 * @param amount The intensity of the refraction distortion
 * @param hasDepthEffect Whether to apply depth-based perspective distortion
 */
data class RefractionEffect(
    @FloatRange(from = 0.0)
    val height: Float,
    @FloatRange(from = 0.0) 
    val amount: Float = height,
    val hasDepthEffect: Boolean = false
) {
    companion object {
        /**
         * Creates a subtle refraction effect suitable for glass buttons
         */
        fun subtle(height: Float = 8f) = RefractionEffect(
            height = height,
            amount = height * 0.6f,
            hasDepthEffect = false
        )
        
        /**
         * Creates a strong refraction effect for dramatic glass surfaces
         */
        fun strong(height: Float = 16f) = RefractionEffect(
            height = height,
            amount = height * 1.2f,
            hasDepthEffect = true
        )
    }
}

/**
 * Configuration for chromatic dispersion effects.
 * This separates light into color components for prismatic effects.
 * 
 * @param height The depth of the dispersion effect in pixels
 * @param amount The intensity of the chromatic separation
 */
data class DispersionEffect(
    @FloatRange(from = 0.0)
    val height: Float,
    @FloatRange(from = 0.0)
    val amount: Float = height
) {
    companion object {
        /**
         * Creates a subtle color separation effect
         */
        fun subtle(height: Float = 6f) = DispersionEffect(
            height = height,
            amount = height * 0.5f
        )
        
        /**
         * Creates a rainbow-like prismatic effect
         */
        fun rainbow(height: Float = 12f) = DispersionEffect(
            height = height,
            amount = height * 1.5f
        )
    }
}

/**
 * Configuration for blur effects.
 * 
 * @param radius The blur radius in pixels
 * @param style The type of blur to apply
 */
data class BlurEffect(
    @FloatRange(from = 0.0)
    val radius: Float,
    val style: BlurStyle = BlurStyle.NORMAL
) {
    enum class BlurStyle {
        NORMAL, SOLID, OUTER, INNER
    }
    
    companion object {
        /**
         * Creates a subtle background blur
         */
        fun subtle(radius: Float = 4f) = BlurEffect(radius, BlurStyle.NORMAL)
        
        /**
         * Creates a strong background blur for focus effects
         */
        fun strong(radius: Float = 12f) = BlurEffect(radius, BlurStyle.NORMAL)
    }
}

/**
 * Configuration for highlight effects.
 * Creates lighting effects that enhance the glass appearance.
 * 
 * @param angle The angle of the light source in radians
 * @param falloff The sharpness of the highlight falloff
 * @param color The color of the highlight
 * @param alpha The opacity of the highlight effect
 */
data class HighlightEffect(
    @FloatRange(from = 0.0, to = 45.0)
    val angle: Float,
    @FloatRange(from = 0.1)
    val falloff: Float = 2f,
    @ColorInt
    val color: Int = Color.WHITE,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 0.3f
) {
    companion object {
        /**
         * Creates a top-left highlight typical for glass interfaces
         */
        fun topLeft(falloff: Float = 2f) = HighlightEffect(
            angle = (Math.PI * 1.25).toFloat(), // 225 degrees
            falloff = falloff
        )
        
        /**
         * Creates a subtle ambient highlight
         */
        fun ambient(alpha: Float = 0.15f) = HighlightEffect(
            angle = (Math.PI * 1.25).toFloat(),
            falloff = 1.5f,
            alpha = alpha
        )
    }
}

/**
 * Configuration for shadow effects.
 * 
 * @param offsetX Horizontal shadow offset in pixels
 * @param offsetY Vertical shadow offset in pixels  
 * @param radius Shadow blur radius in pixels
 * @param color Shadow color
 * @param alpha Shadow opacity
 */
data class ShadowEffect(
    val offsetX: Float,
    val offsetY: Float,
    @FloatRange(from = 0.0)
    val radius: Float,
    @ColorInt
    val color: Int = Color.BLACK,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 0.25f
) {
    companion object {
        /**
         * Creates a subtle drop shadow
         */
        fun drop(offsetY: Float = 4f, radius: Float = 8f) = ShadowEffect(
            offsetX = 0f,
            offsetY = offsetY,
            radius = radius
        )
        
        /**
         * Creates an elevated shadow for floating elements
         */
        fun elevated(offsetY: Float = 8f, radius: Float = 16f) = ShadowEffect(
            offsetX = 0f,
            offsetY = offsetY,
            radius = radius,
            alpha = 0.15f
        )
    }
}

/**
 * Configuration for inner shadow effects.
 * Creates the illusion of depth within the glass surface.
 * 
 * @param offsetX Horizontal inner shadow offset
 * @param offsetY Vertical inner shadow offset
 * @param radius Inner shadow blur radius
 * @param color Inner shadow color
 * @param alpha Inner shadow opacity
 */
data class InnerShadowEffect(
    val offsetX: Float,
    val offsetY: Float,
    @FloatRange(from = 0.0)
    val radius: Float,
    @ColorInt
    val color: Int = Color.BLACK,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 0.2f
) {
    companion object {
        /**
         * Creates a subtle inset effect
         */
        fun inset(offsetY: Float = 2f, radius: Float = 4f) = InnerShadowEffect(
            offsetX = 0f,
            offsetY = offsetY,
            radius = radius
        )
        
        /**
         * Creates a deep carved effect
         */
        fun carved(offsetY: Float = 4f, radius: Float = 6f) = InnerShadowEffect(
            offsetX = 0f,
            offsetY = offsetY,
            radius = radius,
            alpha = 0.3f
        )
    }
}

/**
 * Configuration for color filter effects.
 * Controls brightness, contrast, saturation, and other color adjustments.
 * 
 * @param brightness Brightness adjustment (-1.0 to 1.0)
 * @param contrast Contrast adjustment (0.0 to 2.0, where 1.0 is normal)
 * @param saturation Saturation adjustment (0.0 to 2.0, where 1.0 is normal)
 * @param alpha Opacity adjustment (0.0 to 1.0)
 */
data class ColorFilterEffect(
    @FloatRange(from = -1.0, to = 1.0)
    val brightness: Float = 0f,
    @FloatRange(from = 0.0, to = 2.0)
    val contrast: Float = 1f,
    @FloatRange(from = 0.0, to = 2.0)
    val saturation: Float = 1f,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 1f
) {
    companion object {
        /**
         * Creates a vibrant color filter effect
         */
        fun vibrant() = ColorFilterEffect(saturation = 1.5f)
        
        /**
         * Creates a desaturated (grayscale-like) effect
         */
        fun desaturated() = ColorFilterEffect(saturation = 0.3f)
        
        /**
         * Creates a high contrast effect
         */
        fun highContrast() = ColorFilterEffect(contrast = 1.4f)
        
        /**
         * Creates a bright effect
         */
        fun bright(amount: Float = 0.2f) = ColorFilterEffect(brightness = amount)
        
        /**
         * Creates a dimmed effect
         */
        fun dim(amount: Float = -0.2f) = ColorFilterEffect(brightness = amount)
    }
}

/**
 * Configuration for gamma adjustment effects.
 * Controls the gamma curve for color correction.
 * 
 * @param power Gamma power value (0.1 to 3.0, where 1.0 is normal)
 */
data class GammaAdjustmentEffect(
    @FloatRange(from = 0.1, to = 3.0)
    val power: Float = 1f
) {
    companion object {
        /**
         * Creates a brighter gamma adjustment
         */
        fun brighter() = GammaAdjustmentEffect(power = 0.8f)
        
        /**
         * Creates a darker gamma adjustment
         */
        fun darker() = GammaAdjustmentEffect(power = 1.2f)
    }
}

/**
 * Configuration for exposure adjustment effects.
 * Controls the exposure (brightness) of the image using EV scale.
 * 
 * @param ev Exposure value (-3.0 to 3.0, where 0.0 is normal)
 */
data class ExposureAdjustmentEffect(
    @FloatRange(from = -3.0, to = 3.0)
    val ev: Float = 0f
) {
    companion object {
        /**
         * Creates an overexposed (brighter) effect
         */
        fun overexposed(ev: Float = 1f) = ExposureAdjustmentEffect(ev)
        
        /**
         * Creates an underexposed (darker) effect
         */
        fun underexposed(ev: Float = -1f) = ExposureAdjustmentEffect(ev)
    }
}