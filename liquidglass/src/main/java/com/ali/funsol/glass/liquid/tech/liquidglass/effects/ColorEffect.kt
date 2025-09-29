package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.*
import androidx.annotation.ColorInt

/**
 * An effect that applies color adjustments to a [Bitmap].
 * This includes saturation, brightness, and a color tint.
 */
class ColorEffect : Effect {

    /** The saturation of the image. 1.0 means no change. */
    var saturation: Float = 1f
    /** The brightness of the image. 1.0 means no change. */
    var brightness: Float = 1f
    /** The contrast of the image. 1.0 means no change. */
    var contrast: Float = 1f
    /** A color tint to be applied over the image. */
    @ColorInt var tintColor: Int = Color.TRANSPARENT

    override fun apply(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturation)

        // Add brightness and contrast to the matrix
        val t = (1.0f - contrast) / 2.0f * 255.0f + brightness * 255.0f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, t,
                0f, contrast, 0f, 0f, t,
                0f, 0f, contrast, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )

        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(result, 0f, 0f, paint)

        if (tintColor != Color.TRANSPARENT) {
            paint.colorFilter = null
            paint.color = tintColor
            canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), paint)
        }

        return result
    }
}
