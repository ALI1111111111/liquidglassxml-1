package com.kyant.backdrop.xml

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RenderEffect
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Provides backward compatibility helpers for devices that don't support modern RenderEffect APIs.
 * 
 * API levels:
 * - API 21-30: Basic Canvas drawing with BlurMaskFilter and ColorFilters
 * - API 31 (S): RenderEffect support
 * - API 33 (T): RuntimeShader support
 */
internal object BackwardCompatibility {

    /**
     * Returns true if the device supports RenderEffect (API 31+)
     */
    fun supportsRenderEffect(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * Returns true if the device supports RuntimeShader (API 33+)
     */
    fun supportsRuntimeShader(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Creates a basic glass-like effect using traditional Canvas APIs for older devices.
     * This provides a simplified version of the liquid glass effect.
     */
    fun createLegacyGlassEffect(
        canvas: Canvas,
        path: Path,
        width: Float,
        height: Float,
        blurRadius: Float,
        tintColor: Int = 0
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Modern devices should use RenderEffect
            return
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            
            // Apply basic blur effect using BlurMaskFilter (works on all API levels)
            if (blurRadius > 0f) {
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
            
            // Apply tint if specified
            if (tintColor != 0) {
                colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
            }
            
            // Semi-transparent to create glass-like appearance
            alpha = 200
        }

        canvas.drawPath(path, paint)
    }

    /**
     * Creates a vibrancy effect using ColorMatrix for older devices
     */
    fun createLegacyVibrancyPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Increase saturation using color matrix
            val colorMatrix = android.graphics.ColorMatrix().apply {
                // Saturation: 1.5 for vibrancy effect
                setSaturation(1.5f)
            }
            colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        }
    }

    /**
     * Creates a blur effect using BlurMaskFilter for older devices (API < 31)
     */
    fun createLegacyBlurPaint(radius: Float): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            maskFilter = if (radius > 0f) {
                BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
            } else {
                null
            }
        }
    }

    /**
     * Creates a color adjustment paint for older devices
     */
    fun createLegacyColorControlsPaint(
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f
    ): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val invSat = 1f - saturation
        val r = 0.213f * invSat
        val g = 0.715f * invSat
        val b = 0.072f * invSat

        val c = contrast
        val t = (0.5f - c * 0.5f + brightness) * 255f

        val colorMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                c * (1 - r), c * -g, c * -b, 0f, t,
                c * -r, c * (1 - g), c * -b, 0f, t,
                c * -r, c * -g, c * (1 - b), 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )
        
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        return paint
    }
}
