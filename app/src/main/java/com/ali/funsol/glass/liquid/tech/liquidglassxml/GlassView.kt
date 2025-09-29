package com.ali.funsol.glass.liquid.tech.liquidglassxml

import android.content.Context
import android.graphics.*
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
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
    private var renderScript: RenderScript? = null

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.GlassView)
            blurRadius = a.getFloat(R.styleable.GlassView_blurRadius, blurRadius)
            cornerRadius = a.getDimension(R.style.GlassView_cornerRadius, cornerRadius)
            tintColor = a.getColor(R.styleable.GlassView_tintColor, tintColor)
            saturation = a.getFloat(R.styleable.GlassView_saturation, saturation)
            brightness = a.getFloat(R.styleable.GlassView_brightness, brightness)
            a.recycle()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            renderScript = RenderScript.create(context)
        }
    }

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

        if (safeWidth > 0 && safeHeight > 0) {
            cachedBackground = Bitmap.createBitmap(bitmap, x, y, safeWidth, safeHeight)
        } else {
            cachedBackground = null
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bg = cachedBackground ?: return

        // Step 1: Apply blur or fallback
        val blurred = applyBlur(bg)

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

    private fun applyBlur(src: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // This is not the right way to use RenderEffect.
            // It should be applied to the view itself, not used to create a bitmap.
            // For simplicity, we'll use the RenderScript method for all versions for now.
            return rsBlur(src)
        } else {
            return rsBlur(src)
        }
    }

    private fun rsBlur(src: Bitmap): Bitmap {
        val rs = renderScript ?: return src
        val output = Bitmap.createBitmap(src.width, src.height, src.config)
        val inputAllocation = Allocation.createFromBitmap(rs, src)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blurScript.setRadius(blurRadius.coerceIn(0.1f, 25f))
        blurScript.setInput(inputAllocation)
        blurScript.forEach(outputAllocation)

        outputAllocation.copyTo(output)
        return output
    }

    private fun applyColorAdjustments(src: Bitmap): Bitmap {
        val bitmap = src.copy(src.config, true)
        val canvas = Canvas(bitmap)
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturation)

        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(brightnessMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        paint.colorFilter = null // reset
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBlurS(src: Bitmap): Bitmap {
        // This is a placeholder for a correct Android 12+ implementation
        // For now, we are using RenderScript for all versions.
        return src
    }
}
