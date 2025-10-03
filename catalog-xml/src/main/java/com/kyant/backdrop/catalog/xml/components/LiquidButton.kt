package com.kyant.backdrop.catalog.xml.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.graphics.ShaderBrush
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

    private val liquidGlassContainer: LiquidGlassContainer
    private val buttonText: TextView

    // Animations
    private var offsetAnimation: ValueAnimator? = null
    private var pressStartPosition = PointF(0f, 0f)
    private var currentOffset = PointF(0f, 0f)

    // Spring scale animations
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

    // Highlight animation
    private var highlightProgress = 0f
    private var highlightAnimator: ValueAnimator? = null
    private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var runtimeShader: RuntimeShader? = null

    // Public properties
    var isInteractive = true
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

    init {
        // Glass container
        liquidGlassContainer = LiquidGlassContainer(context)

        // Text inside
        buttonText = TextView(context).apply {
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

        liquidGlassContainer.addView(buttonText, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        addView(liquidGlassContainer, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ))

        // Apply effects
        applyLiquidGlassEffects()

        // Setup interaction
        setupInteraction()

        // Setup highlight shader (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runtimeShader = RuntimeShader(
                """
                uniform float2 size;
                uniform float2 offset;
                uniform float radius;
                uniform float4 color;
                
                half4 main(float2 fragCoord) {
                    float2 uv = fragCoord.xy / size;
                    float2 center = offset / size;
                    float dist = distance(uv, center);
                    float glow = exp(-pow(dist * radius, 2.0));
                    return half4(color.rgb, color.a * glow);
                }
                """
            )
        }

        minimumHeight = (56 * resources.displayMetrics.density).toInt()
    }

    private fun applyLiquidGlassEffects() {
        val density = resources.displayMetrics.density
        liquidGlassContainer.setCornerRadius(28f * density)

        liquidGlassContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        liquidGlassContainer.setBlurEffect(BlurEffect(2f * density))
        liquidGlassContainer.setRefractionEffect(
            RefractionEffect(
                height = 12f * density,
                amount = 24f * density,
                hasDepthEffect = true
            )
        )
        liquidGlassContainer.setHighlightEffect(HighlightEffect(angle = 45f, alpha = 0.15f))
        updateSurfaceEffects()
    }

    private fun setupInteraction() {
        setOnTouchListener { _, event ->
            if (!isInteractive) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressStartPosition.set(event.x, event.y)
                    animatePress(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dragAmount = PointF(
                        event.x - pressStartPosition.x,
                        event.y - pressStartPosition.y
                    )
                    animateOffset(dragAmount)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    animatePress(false)
                    performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    animatePress(false)
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

        // Animate highlight fade in/out
        highlightAnimator?.cancel()
        highlightAnimator = ValueAnimator.ofFloat(
            highlightProgress,
            if (pressed) 1f else 0f
        ).apply {
            duration = 300
            addUpdateListener {
                highlightProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        // Update base highlight effect alpha
        val targetAlpha = if (pressed) 0.25f else 0.15f
        liquidGlassContainer.setHighlightEffect(
            HighlightEffect(angle = 45f, alpha = targetAlpha)
        )
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
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
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
                // Animate back
                ValueAnimator.ofFloat(1f, 0f).apply {
                    duration = 200
                    addUpdateListener { animation ->
                        val progress = animation.animatedValue as Float
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
        if (tintColor != Color.TRANSPARENT || surfaceColor != Color.TRANSPARENT) {
            val brightness = if (surfaceColor != Color.TRANSPARENT) 0.1f else 0f
            val saturation = if (tintColor != Color.TRANSPARENT) 1.2f else 1f
            liquidGlassContainer.setColorFilterEffect(
                ColorFilterEffect(
                    brightness = brightness,
                    saturation = saturation
                )
            )
        } else {
            liquidGlassContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (isInteractive && highlightProgress > 0f) {
            val w = width.toFloat()
            val h = height.toFloat()
            val radius = max(w, h)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
                runtimeShader?.setFloatUniform("size", w, h)
                runtimeShader?.setFloatUniform(
                    "offset",
                    pressStartPosition.x + currentOffset.x,
                    pressStartPosition.y + currentOffset.y
                )
                runtimeShader?.setFloatUniform("radius", radius)
                runtimeShader?.setColorUniform(
                    "color",
                    Color.argb((highlightProgress * 0.15f * 255).toInt(), 255, 255, 255)
                )

                shaderPaint.shader = runtimeShader
                canvas.drawRect(0f, 0f, w, h, shaderPaint)

            } else {
                // 🔄 Better Fallback: Radial Gradient (centered on press point)
                val cx = pressStartPosition.x + currentOffset.x
                val cy = pressStartPosition.y + currentOffset.y
                val gradientRadius = radius * 0.75f

                shaderPaint.shader = RadialGradient(
                    cx, cy, gradientRadius,
                    Color.argb((highlightProgress * 40).toInt(), 255, 255, 255), // center brighter
                    Color.TRANSPARENT, // fade to transparent
                    Shader.TileMode.CLAMP
                )

                canvas.drawRect(0f, 0f, w, h, shaderPaint)
            }
        }
    }



    // Public API
    fun setText(text: String) {
        buttonText.text = text
    }

    fun setTextColor(color: Int) {
        buttonText.setTextColor(color)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
    }

    override fun performClick(): Boolean {
        super.performClick()
        onClickListener?.onClick(this)
        return true
    }
}
