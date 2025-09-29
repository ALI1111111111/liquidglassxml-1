package com.ali.funsol.glass.liquid.tech.liquidglassxml

import android.content.Context
import android.graphics.*
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.graphics.withSave

class GlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var blurRadius: Float = 25f
    var cornerRadius: Float = 0f
    @ColorInt var tintColor: Int = Color.TRANSPARENT
    var saturation: Float = 1f
    var brightness: Float = 1f

    private var cachedBackground: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { captureBackground() }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // recapture on layout change
        post { captureBackground() }
    }

    private fun captureBackground() {
        val parentView = rootView ?: return
        val bitmap = Bitmap.createBitmap(parentView.width, parentView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        parentView.draw(canvas)

        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val x = loc[0]
        val y = loc[1]

        val safeWidth = width.coerceAtMost(bitmap.width - x)
        val safeHeight = height.coerceAtMost(bitmap.height - y)

        cachedBackground = Bitmap.createBitmap(bitmap, x, y, safeWidth, safeHeight)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bg = cachedBackground ?: return

        // Step 1: Apply blur or fallback
        val blurred = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applyBlur(bg)
        } else {
            bg // fallback (no blur yet)
        }

        // Step 2: Apply saturation/brightness
        val adjusted = applyColorAdjustments(blurred)

        // Step 3: Draw with rounded corners
        val path = Path().apply {
            addRoundRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                cornerRadius, cornerRadius,
                Path.Direction.CW
            )
        }

        canvas.withSave {
            clipPath(path)
            drawBitmap(adjusted, 0f, 0f, paint)


            if (tintColor != Color.TRANSPARENT) {
                tintPaint.color = tintColor
                drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBlur(src: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On Android 12+, apply RenderEffect directly on this view
            setRenderEffect(
                RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
            )
            return src // no need to create new bitmap, GPU handles it
        } else {
            // Fallback: return original until we implement stack blur
            return src
        }
    }


    private fun applyColorAdjustments(src: Bitmap): Bitmap {
        val cm = ColorMatrix()
        cm.setSaturation(saturation)

        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(brightnessMatrix)

        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }
}
