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
        // This effect is handled differently, in a separate drawing pass.
        // This apply method will not be called.
        return bitmap
    }

    fun draw(canvas: Canvas) {
        val currentPath = path ?: return
        if (radius <= 0) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@OuterShadowEffect.color
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.save()
        canvas.translate(dx, dy)
        canvas.drawPath(currentPath, paint)
        canvas.restore()
    }
}
