package com.kyant.backdrop.xml

import android.graphics.ColorFilter
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Represents a dynamic effect that can be applied to a [LiquidGlassView] at runtime.
 */
@RequiresApi(Build.VERSION_CODES.S)
sealed class BackdropEffect {
    /**
     * Whether this effect requires the view's global coordinates on the screen to be passed
     * to its shader. If true, the system will do extra work to track the view's position.
     */
    open val needsGlobalCoordinates: Boolean = false

    /**
     * Creates the underlying [RenderEffect] for this effect.
     * @return The created [RenderEffect].
     */
    internal abstract fun createRenderEffect(): RenderEffect

    /**
     * A blur effect.
     * @property radiusX The radius of the blur in the horizontal direction.
     * @property radiusY The radius of the blur in the vertical direction.
     */
    data class Blur(val radiusX: Float, val radiusY: Float) : BackdropEffect() {
        override fun createRenderEffect(): RenderEffect {
            return RenderEffect.createBlurEffect(radiusX, radiusY, Shader.TileMode.CLAMP)
        }
    }

    /**
     * A color filter effect.
     * @property colorFilter The [ColorFilter] to apply.
     */
    data class ColorFilter(val colorFilter: android.graphics.ColorFilter) : BackdropEffect() {
        override fun createRenderEffect(): RenderEffect {
            return RenderEffect.createColorFilterEffect(colorFilter)
        }
    }

    /**
     * A saturation effect.
     * @property saturation The saturation level. 0.0 is grayscale, 1.0 is original.
     */
    data object Vibrancy : BackdropEffect() {
        override fun createRenderEffect(): RenderEffect {
            return RenderEffect.createColorFilterEffect(VibrantColorFilter)
        }
    }

    /**
     * A noise effect.
     * @property intensity The intensity of the noise.
     */
    @RequiresApi(33)
    data class Noise(val intensity: Float) : BackdropEffect() {
        override fun createRenderEffect(): RenderEffect {
            val shader = ShaderCache.get(NoiseShaderString)
            shader.setFloatUniform("intensity", intensity)
            return RenderEffect.createShaderEffect(shader)
        }
    }

    /**
     * An inverse drawing effect that "cuts out" a shape from the backdrop,
     * revealing what is underneath. This is a direct canvas operation, not a RenderEffect.
     * @property path The path defining the shape to cut out.
     */
    data class Inverse(val path: Path) : BackdropEffect() {
        // This effect does not create a RenderEffect. It's handled specially.
        override fun createRenderEffect(): RenderEffect {
            throw UnsupportedOperationException("Inverse is a canvas operation and does not create a RenderEffect.")
        }
    }
}

private val VibrantColorFilter = colorControlsColorFilter(saturation = 1.5f)

private fun colorControlsColorFilter(
    brightness: Float = 0f,
    contrast: Float = 1f,
    saturation: Float = 1f
): ColorFilter {
    val invSat = 1f - saturation
    val r = 0.213f * invSat
    val g = 0.715f * invSat
    val b = 0.072f * invSat

    val c = contrast
    val t = (0.5f - c * 0.5f + brightness) * 255f
    val s = saturation

    val cr = c * r
    val cg = c * g
    val cb = c * b
    val cs = c * s

    val colorMatrix = android.graphics.ColorMatrix(
        floatArrayOf(
            cr + cs, cg, cb, 0f, t,
            cr, cg + cs, cb, 0f, t,
            cr, cg, cb + cs, 0f, t,
            0f, 0f, 0f, 1f, 0f
        )
    )
    return android.graphics.ColorMatrixColorFilter(colorMatrix)
}
