package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.*
import androidx.annotation.ColorInt
import kotlin.math.pow

/**
 * An effect that applies color adjustments to a [Bitmap].
 * This includes saturation, brightness, and a color tint.
 */
class ColorEffect : Effect {

    /** The saturation of the image. 1.0 means no change. */
    var saturation: Float = 1f
    /** The vibrancy of the image. 1.0 means no change, values > 1.0 increase saturation. */
    var vibrancy: Float = 1f
    /** The brightness of the image. 1.0 means no change. */
    var brightness: Float = 0f
    /** The contrast of the image. 1.0 means no change. */
    var contrast: Float = 1f
    /** The exposure of the image. EV value, 0 means no change. */
    var exposure: Float = 0f
    /** A color tint to be applied over the image. */
    @ColorInt var tintColor: Int = Color.TRANSPARENT

    override fun apply(bitmap: Bitmap): Bitmap {
        if (saturation == 1f && vibrancy == 1f && brightness == 0f && contrast == 1f && tintColor == Color.TRANSPARENT && exposure == 0f) {
            return bitmap
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        // Saturation matrix
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation * vibrancy)

        // Brightness matrix
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness * 255f,
                0f, 1f, 0f, 0f, brightness * 255f,
                0f, 0f, 1f, 0f, brightness * 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Contrast matrix
        val t = (1.0f - contrast) / 2.0f * 255.0f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, t,
                0f, contrast, 0f, 0f, t,
                0f, 0f, contrast, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Exposure matrix
        val exposureScale = 2f.pow(exposure / 2.2f)
        val exposureMatrix = ColorMatrix().apply {
            setScale(exposureScale, exposureScale, exposureScale, 1f)
        }

        val colorMatrix = ColorMatrix()
        colorMatrix.postConcat(saturationMatrix)
        colorMatrix.postConcat(contrastMatrix)
        colorMatrix.postConcat(brightnessMatrix)
        colorMatrix.postConcat(exposureMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        if (tintColor != Color.TRANSPARENT) {
            val tintPaint = Paint().apply {
                color = tintColor
            }
            canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), tintPaint)
        }

        return result
    }
}
