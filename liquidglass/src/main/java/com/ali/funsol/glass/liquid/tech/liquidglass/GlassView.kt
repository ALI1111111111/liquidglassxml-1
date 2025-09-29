package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.BlurEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.ColorEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.DispersionEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.Effect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.HighlightEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.InnerShadowEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.OuterShadowEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.RefractionEffect

/**
 * A custom [View] that applies a "liquid glass" effect to the content behind it.
 * The effect is a combination of blur, color adjustments, and refraction.
 *
 * This view can be configured via XML attributes or programmatically.
 */
class GlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val effects = mutableListOf<Effect>()
    private val blurEffect = BlurEffect(context)
    private val colorEffect = ColorEffect()
    private val refractionEffect = RefractionEffect(context)
    private val dispersionEffect = DispersionEffect(context)
    private val highlightEffect = HighlightEffect()
    private val innerShadowEffect = InnerShadowEffect()
    private val outerShadowEffect = OuterShadowEffect()

    /** The corner radius of the view, used for clipping. */
    var cornerRadius: Float = 0f
    private var clipPath: Path? = null
    private var surfaceColor: Int = Color.TRANSPARENT

    private var cachedBackground: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val camera = Camera()

    // --- Transformation Properties ---
    var layerScaleX: Float = 1f
    var layerScaleY: Float = 1f
    var layerTranslationX: Float = 0f
    var layerTranslationY: Float = 0f
    var layerRotation: Float = 0f
    var layerRotationX: Float = 0f
    var layerRotationY: Float = 0f

    /** Public property to control the blur radius at runtime. */
    var blurRadius: Float
        get() = blurEffect.radius
        set(value) {
            blurEffect.radius = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (value > 0) {
                    setRenderEffect(RenderEffect.createBlurEffect(value, value, Shader.TileMode.CLAMP))
                } else {
                    setRenderEffect(null)
                }
            }
            invalidate()
        }

    /** Public property to control the color saturation at runtime. */
    var saturation: Float
        get() = colorEffect.saturation
        set(value) {
            colorEffect.saturation = value
            invalidate()
        }

    /** Public property to control the color brightness at runtime. */
    var brightness: Float
        get() = colorEffect.brightness
        set(value) {
            colorEffect.brightness = value
            invalidate()
        }

    /** Public property to control the refraction intensity at runtime. */
    var refractionIntensity: Float
        get() = refractionEffect.intensity
        set(value) {
            refractionEffect.intensity = value
            invalidate()
        }

    /** Public property to control the dispersion intensity at runtime. */
    var dispersionIntensity: Float
        get() = dispersionEffect.intensity
        set(value) {
            dispersionEffect.intensity = value
            invalidate()
        }

    /** Public property to control the color contrast at runtime. */
    var contrast: Float
        get() = colorEffect.contrast
        set(value) {
            colorEffect.contrast = value
            invalidate()
        }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.GlassView)
            blurRadius = a.getFloat(R.styleable.GlassView_blurRadius, blurRadius)
            cornerRadius = a.getDimension(R.styleable.GlassView_cornerRadius, cornerRadius)
            colorEffect.tintColor = a.getColor(R.styleable.GlassView_tintColor, colorEffect.tintColor)
            saturation = a.getFloat(R.styleable.GlassView_saturation, saturation)
            brightness = a.getFloat(R.styleable.GlassView_brightness, brightness)
            contrast = a.getFloat(R.styleable.GlassView_contrast, contrast)
            refractionIntensity = a.getFloat(R.styleable.GlassView_refractionIntensity, refractionIntensity)
            dispersionIntensity = a.getFloat(R.styleable.GlassView_dispersionIntensity, dispersionIntensity)
            innerShadowEffect.radius = a.getDimension(R.styleable.GlassView_innerShadowRadius, innerShadowEffect.radius)
            innerShadowEffect.color = a.getColor(R.styleable.GlassView_innerShadowColor, innerShadowEffect.color)
            surfaceColor = a.getColor(R.styleable.GlassView_surfaceColor, surfaceColor)
            refractionEffect.hasDepthEffect = a.getBoolean(R.styleable.GlassView_hasDepthEffect, refractionEffect.hasDepthEffect)
            val styleIndex = a.getInt(R.styleable.GlassView_highlightStyle, 0)
            highlightEffect.style = HighlightEffect.Style.values()[styleIndex]
            highlightEffect.highlightAngle = a.getFloat(R.styleable.GlassView_highlightAngle, highlightEffect.highlightAngle)
            outerShadowEffect.radius = a.getDimension(R.styleable.GlassView_outerShadowRadius, outerShadowEffect.radius)
            outerShadowEffect.color = a.getColor(R.styleable.GlassView_outerShadowColor, outerShadowEffect.color)
            outerShadowEffect.dx = a.getDimension(R.styleable.GlassView_outerShadowDx, outerShadowEffect.dx)
            outerShadowEffect.dy = a.getDimension(R.styleable.GlassView_outerShadowDy, outerShadowEffect.dy)
            a.recycle()
        }

        // The order of effects matters.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            effects.add(blurEffect)
        }
        effects.add(refractionEffect)
        effects.add(dispersionEffect)
        effects.add(colorEffect)
        effects.add(highlightEffect)
        effects.add(innerShadowEffect)
        // Outer shadow is not in the main effects list as it's drawn separately
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Crucial step to prevent memory leaks from RenderScript.
        blurEffect.destroy()
        refractionEffect.destroy()
        dispersionEffect.destroy()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
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
        var bg = cachedBackground ?: return

        val path = clipPath ?: Path().apply {
            addRoundRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                cornerRadius, cornerRadius,
                Path.Direction.CW
            )
        }
        innerShadowEffect.path = path
        outerShadowEffect.path = path

        // --- Drawing Passes ---

        // 1. Draw Outer Shadow (Behind)
        outerShadowEffect.draw(canvas)

        // 2. Process and Draw Main Effects Bitmap
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            for (effect in effects) {
                bg = effect.apply(bg)
            }
        } else {
            for (effect in effects.filter { it !is BlurEffect }) {
                bg = effect.apply(bg)
            }
        }

        canvas.withSave {
            clipPath(path)

            // Apply layer transformations
            val centerX = width / 2f
            val centerY = height / 2f

            camera.save()
            camera.rotateX(layerRotationX)
            camera.rotateY(layerRotationY)
            val matrix = Matrix()
            camera.getMatrix(matrix)
            camera.restore()

            matrix.preTranslate(-centerX, -centerY)
            matrix.postTranslate(centerX, centerY)
            canvas.concat(matrix)

            canvas.translate(layerTranslationX, layerTranslationY)
            canvas.rotate(layerRotation, centerX, centerY)
            canvas.scale(layerScaleX, layerScaleY, centerX, centerY)

            drawBitmap(bg, 0f, 0f, paint)

            // 3. Draw Surface Color Layer (On Top)
            if (surfaceColor != Color.TRANSPARENT) {
                surfacePaint.color = surfaceColor
                drawRect(0f, 0f, width.toFloat(), height.toFloat(), surfacePaint)
            }
        }
    }

    /**
     * Sets the properties of the simulated highlight for TOUCH style.
     *
     * @param center The center position of the highlight.
     * @param radius The radius of the highlight.
     */
    fun setTouchHighlight(center: PointF, radius: Float) {
        highlightEffect.highlightCenter = center
        highlightEffect.highlightRadius = radius
        invalidate()
    }

    /**
     * Sets a custom [Path] to clip the view's contents.
     * This allows for rendering the glass effect in non-rectangular shapes.
     *
     * @param path The path to use for clipping.
     */
    fun setClipPath(path: Path) {
        this.clipPath = path
        invalidate()
    }
}
