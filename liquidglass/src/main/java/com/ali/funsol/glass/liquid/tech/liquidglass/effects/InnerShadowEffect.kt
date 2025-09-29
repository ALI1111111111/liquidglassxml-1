package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.*

/**
 * An effect that draws an inner shadow inside a given path on a [Bitmap].
 * This is used to create a sense of depth.
 */
class InnerShadowEffect : Effect {

    /** The path that defines the shape of the inner shadow. */
    var path: Path? = null
    /** The radius of the shadow's blur. */
    var radius: Float = 10f
    /** The color of the shadow. */
    var color: Int = Color.argb(50, 0, 0, 0)

    override fun apply(bitmap: Bitmap): Bitmap {
        val currentPath = path ?: return bitmap
        if (radius <= 0) return bitmap

        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@InnerShadowEffect.color
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }

        // Create a temporary bitmap for the shadow
        val shadowBitmap = Bitmap.createBitmap(result.width, result.height, Bitmap.Config.ARGB_8888)
        val shadowCanvas = Canvas(shadowBitmap)

        // Draw the shadow shape inverted
        shadowCanvas.drawPath(currentPath, paint)

        // Use a PorterDuff mode to "cut out" the center, leaving only the inner shadow
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        shadowCanvas.drawPath(currentPath, paint)

        // Draw the resulting inner shadow onto the original bitmap
        canvas.drawBitmap(shadowBitmap, 0f, 0f, null)

        return result
    }
}
