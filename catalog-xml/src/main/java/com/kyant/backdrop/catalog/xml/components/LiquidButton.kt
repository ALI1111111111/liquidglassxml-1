package com.kyant.backdrop.catalog.xml.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import kotlin.math.*

class LiquidButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Main Glass Container
    private val liquidGlassContainer: LiquidGlassContainer = LiquidGlassContainer(context)

    // TextView in center
    internal val buttonText: TextView = TextView(context).apply {
        textSize = 15f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        text = "Liquid Button"
        typeface = Typeface.DEFAULT_BOLD
        setPadding(
            (16 * resources.displayMetrics.density).toInt(),
            (12 * resources.displayMetrics.density).toInt(),
            (16 * resources.displayMetrics.density).toInt(),
            (12 * resources.displayMetrics.density).toInt()
        )
    }
    private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var offsetAnimation: ValueAnimator? = null
    private var pressStartPosition = PointF(0f, 0f)
    private var currentOffset = PointF(0f, 0f)

    private val springScaleX = SpringAnimation(this, SpringAnimation.SCALE_X, 1f).apply {
        spring = SpringForce(1f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }
    }

    private val springScaleY = SpringAnimation(this, SpringAnimation.SCALE_Y, 1f).apply {
        spring = SpringForce(1f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }
    }

    private var highlightProgress = 0f
    private var highlightAnimator: ValueAnimator? = null
    private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var runtimeShader: RuntimeShader? = null
    private var timeAnimator: ValueAnimator? = null
    private var time = 0f

    var isInteractive = true
    
    // Backdrop layer reference for real-time updates
    private var backdropLayerCallback: (() -> Unit)? = null

    var tintColor = Color.TRANSPARENT
        set(value) {
            field = value
            updateSurfaceEffects()
        }

    var surfaceColor = Color.TRANSPARENT
        set(value) {
            field = value
            updateSurfaceEffects()
        }

    private var onClickListener: OnClickListener? = null
    
    /**
     * Sets the backdrop source for the liquid glass effect
     */
    fun setBackdropSource(backdrop: com.kyant.backdrop.xml.backdrop.XmlBackdrop) {
        liquidGlassContainer.setBackgroundSource(backdrop)
    }
    
    /**
     * Sets the backdrop source from a LayerBackdropView
     * This also enables real-time backdrop updates during touch interactions
     */
    fun setBackdropSource(backdropView: com.kyant.backdrop.xml.backdrop.LayerBackdropView) {
        liquidGlassContainer.setBackgroundSource(backdropView.getBackdrop())
        // Register backdrop invalidation callback for real-time updates during press
        backdropLayerCallback = { backdropView.invalidateLayer() }
    }

    init {

        liquidGlassContainer.addView(
            buttonText,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )

        addView(liquidGlassContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        applyLiquidGlassEffects()
        setupInteraction()
        setupShader()
        startTimeAnimation()

        minimumHeight = (56 * resources.displayMetrics.density).toInt()
        clipToOutline = true
        clipChildren = true
    }

    //  Initialize the enhanced shader
    private fun setupShader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runtimeShader = RuntimeShader(
                """
                uniform float2 size;
                uniform float2 offset;
                uniform float radius;
                uniform float time;
                uniform half4 color;

                float noise(float2 p) {
                    return fract(sin(dot(p, float2(12.9898,78.233))) * 43758.5453);
                }

                float smoothNoise(float2 p) {
                    float2 i = floor(p);
                    float2 f = fract(p);
                    float a = noise(i);
                    float b = noise(i + float2(1.0, 0.0));
                    float c = noise(i + float2(0.0, 1.0));
                    float d = noise(i + float2(1.0, 1.0));
                    float2 u = f * f * (3.0 - 2.0 * f);
                    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
                }

                half4 main(float2 fragCoord) {
                    float2 uv = fragCoord / size;
                    float2 center = offset / size;
                    float2 delta = uv - center;
                    float dist = length(delta);

                    // Ripple distortion
                    float ripple = sin(dist * radius * 0.4 - time * 3.0) * 0.06;
                    float2 distortedUV = uv + normalize(delta) * ripple;

                    // Subtle glass shimmer
                    float shimmer = smoothNoise(uv * 6.0 + time * 0.6) * 0.15;

                    // Chromatic aberration
                    float3 baseColor = float3(
                        0.9 + 0.1 * sin(time + distortedUV.x * 6.2831),
                        0.95 + 0.05 * sin(time + distortedUV.y * 6.2831),
                        1.0
                    );

                    // Central glow for press highlight
                    float glow = exp(-pow(dist * radius * 0.45, 2.0)) * 1.1;

                    float alpha = color.a * (glow + shimmer);
                    return half4(baseColor * color.rgb, alpha);
                }
                """
            )
        }
    }

    //  Animate shader time for live shimmer
    private fun startTimeAnimation() {
        timeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                time += 0.02f
                invalidate()
            }
            start()
        }
    }

    // Glass effects setup
    private fun applyLiquidGlassEffects() {
        val density = resources.displayMetrics.density
        liquidGlassContainer.setCornerRadius(28f * density)
        liquidGlassContainer.setColorFilterEffect(ColorFilterEffect(brightness = 0.18f, saturation = 1.1f))
        liquidGlassContainer.setBlurEffect(BlurEffect(2f * density))
        liquidGlassContainer.setRefractionEffect(
            RefractionEffect(height = 6f * density, amount = 12f * density, hasDepthEffect = false)
        )
        liquidGlassContainer.setHighlightEffect(HighlightEffect(angle = 45f, alpha = 0.15f))
        updateSurfaceEffects()
    }

    //  Handle touch and press animations
    private fun setupInteraction() {
        setOnTouchListener { _, event ->
            if (!isInteractive) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressStartPosition.set(event.x, event.y)
                    animatePress(true)
                    // Force backdrop update on press for real-time glass effects
                    backdropLayerCallback?.invoke()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dragAmount = PointF(event.x - pressStartPosition.x, event.y - pressStartPosition.y)
                    animateOffset(dragAmount)
                    // Force backdrop update during move
                    backdropLayerCallback?.invoke()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    animatePress(false)
                    performClick()
                    // Force backdrop update on release
                    backdropLayerCallback?.invoke()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    animatePress(false)
                    // Force backdrop update on cancel
                    backdropLayerCallback?.invoke()
                    true
                }

                else -> false
            }
        }

        isClickable = true
        isFocusable = true
    }

    private fun animatePress(pressed: Boolean) {
        val targetScale = if (pressed) 0.9f else 1f
        springScaleX.animateToFinalPosition(targetScale)
        springScaleY.animateToFinalPosition(targetScale)

        highlightAnimator?.cancel()
        highlightAnimator = ValueAnimator.ofFloat(highlightProgress, if (pressed) 1f else 0f).apply {
            duration = 250
            addUpdateListener {
                highlightProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        val targetAlpha = if (pressed) 0.25f else 0.15f
        liquidGlassContainer.setHighlightEffect(HighlightEffect(angle = 45f, alpha = targetAlpha))
    }

    private fun animateOffset(dragAmount: PointF) {
        if (!isInteractive) return
        offsetAnimation?.cancel()

        val maxOffset = 8f * resources.displayMetrics.density
        val dampedOffset = PointF(
            tanh(dragAmount.x / maxOffset) * maxOffset * 0.5f,
            tanh(dragAmount.y / maxOffset) * maxOffset * 0.5f
        )

        offsetAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 100
            addUpdateListener {
                val progress = it.animatedValue as Float
                val targetOffset = PointF(
                    dampedOffset.x * progress,
                    dampedOffset.y * progress
                )
                liquidGlassContainer.translationX = targetOffset.x
                liquidGlassContainer.translationY = targetOffset.y
                currentOffset.set(targetOffset.x, targetOffset.y)
                invalidate()
            }
            doOnEnd {
                ValueAnimator.ofFloat(1f, 0f).apply {
                    duration = 200
                    addUpdateListener { anim ->
                        val progress = anim.animatedValue as Float
                        liquidGlassContainer.translationX = currentOffset.x * progress
                        liquidGlassContainer.translationY = currentOffset.y * progress
                        invalidate()
                    }
                    start()
                }
            }
            start()
        }
    }

    private fun updateSurfaceEffects() {
        val hasTint = tintColor != Color.TRANSPARENT
        val hasSurface = surfaceColor != Color.TRANSPARENT
        val brightness = if (hasSurface) 0.25f else 0.15f
        val saturation = if (hasTint) 1.3f else 1.1f

        liquidGlassContainer.setColorFilterEffect(ColorFilterEffect(brightness = brightness, saturation = saturation))
        tintPaint.color = tintColor
        tintPaint.alpha = when {
            hasTint -> 190
            hasSurface -> 100
            else -> 0
        }
        tintPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (tintPaint.alpha > 0) {
            val radius = 28f * resources.displayMetrics.density
            val overlayRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawRoundRect(overlayRect, radius, radius, tintPaint)
        }

        super.dispatchDraw(canvas)

        // Reflection gradient
        val gradient = LinearGradient(
            0f, 0f, 0f, height * 0.6f,
            Color.argb(80, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        val reflectionPaint = Paint().apply {
            shader = gradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        }
        val corner = 28f * resources.displayMetrics.density
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), corner, corner, reflectionPaint)

        drawHighlightEffect(canvas)
    }

    private fun drawHighlightEffect(canvas: Canvas) {
        if (!isInteractive || highlightProgress <= 0f) return

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = max(w, h)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
            try {
                runtimeShader?.apply {
                    setFloatUniform("size", w, h)
                    setFloatUniform("offset", pressStartPosition.x + currentOffset.x, pressStartPosition.y + currentOffset.y)
                    setFloatUniform("radius", radius)
                    setFloatUniform("time", time)
                    setColorUniform("color", Color.valueOf(1f, 1f, 1f, 0.3f * highlightProgress))
                }
                shaderPaint.shader = runtimeShader
                canvas.drawRoundRect(0f, 0f, w, h, 28f, 28f, shaderPaint)
            } catch (_: Exception) {
                drawFallbackHighlight(canvas, w, h)
            }
        } else {
            drawFallbackHighlight(canvas, w, h)
        }
    }

    private fun drawFallbackHighlight(canvas: Canvas, w: Float, h: Float) {
        val cx = pressStartPosition.x + currentOffset.x
        val cy = pressStartPosition.y + currentOffset.y
        val gradientRadius = max(w, h) * 0.75f
        shaderPaint.shader = RadialGradient(
            cx, cy, gradientRadius,
            Color.argb((highlightProgress * 80).toInt(), 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(0f, 0f, w, h, 28f, 28f, shaderPaint)
    }

    fun setText(text: String) = buttonText.setText(text)
    fun setTextColor(color: Int) = buttonText.setTextColor(color)

    override fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
    }

    override fun performClick(): Boolean {
        super.performClick()
        onClickListener?.onClick(this)
        return true
    }
}
