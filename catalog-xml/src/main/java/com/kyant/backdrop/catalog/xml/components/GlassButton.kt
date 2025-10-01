package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.utils.InteractiveGlassGestureHelper
import com.kyant.backdrop.xml.BackdropEffect
import com.kyant.backdrop.xml.LiquidGlassView

@RequiresApi(Build.VERSION_CODES.M)
class GlassButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    private val button: MaterialButton
    private val gestureHelper: InteractiveGlassGestureHelper
    private var isInteractive: Boolean = true

    var text: CharSequence?
        get() = button.text
        set(value) {
            button.text = value
        }

    @ColorInt
    private var tintColor: Int = Color.TRANSPARENT
    @ColorInt
    private var surfaceColor: Int = Color.TRANSPARENT
    
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 0
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }
    
    private var currentProgress = 0f
    private var highlightX = 0f
    private var highlightY = 0f

    init {
        // Inflate the button layout and attach it to this view
        LayoutInflater.from(context).inflate(R.layout.glass_button_internal, this, true)
        button = findViewById(R.id.internal_button)

        // Apply custom attributes
        context.withStyledAttributes(attrs, R.styleable.GlassButton) {
            text = getString(R.styleable.GlassButton_android_text)
            tintColor = getColor(R.styleable.GlassButton_tint, Color.TRANSPARENT)
            surfaceColor = getColor(R.styleable.GlassButton_surfaceColor, Color.TRANSPARENT)
            isInteractive = getBoolean(R.styleable.GlassButton_interactive, true)
        }

        // Set default glass properties
        animationDuration = 200
        setCornerRadius(24f * resources.displayMetrics.density)
        setRefraction(
            height = 12f * resources.displayMetrics.density,
            amount = 24f * resources.displayMetrics.density,
            depthEffect = 0.1f
        )
        setHighlight(
            angle = 2.5f,
            falloff = 1.5f,
            type = com.kyant.backdrop.xml.HighlightType.SPECULAR
        )
        
        // Add shadow for depth (matching Compose version)
        setShadow(com.kyant.backdrop.xml.DefaultShadow(
            elevation = 4f * resources.displayMetrics.density,
            color = 0x0D000000.toInt(), // 5% black
            offsetX = 0f,
            offsetY = 2f * resources.displayMetrics.density
        ))
        
        // Add inner shadow for depth (matching Compose LiquidButton)
        setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
            elevation = 8f * resources.displayMetrics.density,
            color = 0x1A000000.toInt(), // 10% black
            offsetX = 0f,
            offsetY = 0f
        ))

        // Add vibrancy and blur effects on modern devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addVibrancyEffect()
            addBlurEffect(2f * resources.displayMetrics.density)
        }

        // Setup tint and surface color
        if (tintColor != Color.TRANSPARENT) {
            button.setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(tintColor)
        }

        if (surfaceColor != Color.TRANSPARENT) {
            foreground = ColorDrawable(surfaceColor)
        }

        // Setup interactive gesture handling
        gestureHelper = InteractiveGlassGestureHelper(
            this,
            InteractiveGlassGestureHelper.Config(
                enableScale = isInteractive,
                enableTranslation = isInteractive,
                maxScale = 0.1f,
                maxOffset = 50f, // Fixed offset value
                animationDuration = 200L
            )
        )

        gestureHelper.onAnimationUpdate = { progress, translationX, translationY, scaleX, scaleY ->
            currentProgress = progress
            
            if (isInteractive) {
                // Update glass effect parameters based on interaction
                val pressedRefractionHeight = 24f * resources.displayMetrics.density
                val idleRefractionHeight = 12f * resources.displayMetrics.density
                val refractionHeight = idleRefractionHeight + (pressedRefractionHeight - idleRefractionHeight) * progress
                
                val pressedRefractionAmount = 32f * resources.displayMetrics.density
                val idleRefractionAmount = 24f * resources.displayMetrics.density
                val refractionAmount = idleRefractionAmount + (pressedRefractionAmount - idleRefractionAmount) * progress

                setRefraction(refractionHeight, refractionAmount, 0.1f + 0.1f * progress)
                
                // Update highlight
                val pressedFalloff = 0.5f
                val idleFalloff = 1.5f
                val falloff = idleFalloff + (pressedFalloff - idleFalloff) * progress
                setHighlight(2.5f, falloff, com.kyant.backdrop.xml.HighlightType.SPECULAR)

                // Apply transformations
                setBackdropScaleX(scaleX)
                setBackdropScaleY(scaleY)
                setBackdropTranslationX(translationX)
                setBackdropTranslationY(translationY)
            }

            invalidate()
        }

        // Add custom surface drawing - LIGHT frosted glass surface (matching Compose)
        onDrawSurface = { canvas ->
            // For API <31: Add subtle fallback surface for visibility
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val fallbackPaint = Paint().apply {
                    color = Color.WHITE
                    // Light opacity - let blur show on modern devices
                    alpha = when {
                        tintColor != Color.TRANSPARENT -> 38 // 15% for tinted
                        surfaceColor != Color.TRANSPARENT -> 38 // 15% for surface
                        else -> 26 // 10% for transparent buttons
                    }
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fallbackPaint)
            } else {
                // Modern devices: very light surface - blur and highlight do the work
                val paint = Paint().apply {
                    color = Color.WHITE
                    alpha = 26 // 10% base surface
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            
            // Draw tint if specified (matches Compose: subtle overlay)
            if (tintColor != Color.TRANSPARENT) {
                val paint = Paint().apply {
                    color = tintColor
                    alpha = 51 // 20% opacity for tint
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }

            // Draw surface color if specified
            if (surfaceColor != Color.TRANSPARENT) {
                val paint = Paint().apply {
                    color = surfaceColor
                    alpha = 51 // 20% opacity
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }

            // Draw interactive highlight
            if (isInteractive && currentProgress > 0f) {
                highlightPaint.alpha = (25 * currentProgress).toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), highlightPaint)
                
                // Draw radial highlight at press position
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val highlightRadius = (width.coerceAtLeast(height) * 0.8f) * currentProgress
                    highlightPaint.alpha = (38 * currentProgress).toInt()
                    canvas.drawCircle(highlightX, highlightY, highlightRadius, highlightPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractive) {
            return super.onTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            highlightX = event.x
            highlightY = event.y
        }

        gestureHelper.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        button.setOnClickListener(l)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        button.isEnabled = enabled
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        gestureHelper.cleanup()
    }
}
