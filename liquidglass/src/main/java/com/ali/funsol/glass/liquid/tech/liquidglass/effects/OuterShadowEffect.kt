package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.*

/**
 * An effect that draws an outer drop shadow behind a given path on a [Bitmap].
 */
class OuterShadowEffect : Effect {

    /** The path that defines the shape of the outer shadow. */
    var path: Path? = null
    /** The radius of the shadow's blur. */
    var radius: Float = 10f
    /** The color of the shadow. */
    var color: Int = Color.argb(50, 0, 0, 0)
    /** The horizontal offset of the shadow. */
    var dx: Float = 0f
    /** The vertical offset of the shadow. */
    var dy: Float = 0f

    override fun apply(bitmap: Bitmap): Bitmap {
        val currentPath = path ?: return bitmap
        if (radius <= 0 && dx == 0f && dy == 0f) return bitmap

        // Create a new bitmap that is large enough to contain the shadow
        val shadowBitmap = Bitmap.createBitmap(
            (bitmap.width + radius * 2 + dx.let { if (it > 0) it else -it }).toInt(),
            (bitmap.height + radius * 2 + dy.let { if (it > 0) it else -it }).toInt(),
            bitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val shadowCanvas = Canvas(shadowBitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@OuterShadowEffect.color
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }

        shadowCanvas.save()
        shadowCanvas.translate(radius + dx, radius + dy)
        shadowCanvas.drawPath(currentPath, paint)
        shadowCanvas.restore()

        // Draw the original bitmap on top of the shadow
        shadowCanvas.drawBitmap(bitmap, radius, radius, null)

        return shadowBitmap
    }
}
