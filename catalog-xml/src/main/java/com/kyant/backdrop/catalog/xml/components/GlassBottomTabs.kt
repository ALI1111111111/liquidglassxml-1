package com.kyant.backdrop.catalog.xml.components

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.annotation.MenuRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.BackdropEffect
import com.kyant.backdrop.xml.HighlightType
import com.kyant.backdrop.xml.LiquidGlassView

/**
 * Enhanced glass bottom tabs with smooth animations and interactive effects.
 */
class GlassBottomTabs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val liquidGlassView: LiquidGlassView
    val bottomNavigationView: BottomNavigationView
    
    private val selectionAnimator = ValueAnimator().apply {
        duration = 300
        interpolator = OvershootInterpolator(0.8f)
    }
    
    private var currentHighlightAngle = 2.5f

    init {
        inflate(context, R.layout.glass_bottom_tabs, this)
        liquidGlassView = findViewById(R.id.tabs_background)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        val density = resources.displayMetrics.density
        
        liquidGlassView.apply {
            setCornerRadii(
                topLeft = 24 * density,
                topRight = 24 * density,
                bottomRight = 0f,
                bottomLeft = 0f
            )
            
            // Add enhanced effects on modern devices
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addVibrancyEffect()
                addBlurEffect(24 * density)
            }
            
            // ALWAYS draw container surface (matching Compose: 10-15% opacity for frosted glass)
            onDrawSurface = { canvas ->
                // Light theme: #FAFAFA 15%, Dark theme: #121212 15%
                // Use light theme default for now
                val containerPaint = android.graphics.Paint().apply {
                    color = 0xFFFAFAFA.toInt() // Light gray
                    alpha = 38 // 15% opacity for frosted glass effect
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), containerPaint)
            }
            
            setRefraction(16 * density, 24 * density, 0.1f)
            setHighlight(2.5f, 1.5f, HighlightType.SPECULAR)
            
            // Add shadow for depth (matching Compose)
            setShadow(com.kyant.backdrop.xml.DefaultShadow(
                elevation = 8 * density,
                color = 0x0D000000.toInt(), // 5% black
                offsetX = 0f,
                offsetY = -4 * density
            ))
            
            // Add inner shadow for depth (matching Compose)
            setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
                elevation = 12 * density,
                color = 0x1A000000.toInt(), // 10% black
                offsetX = 0f,
                offsetY = 0f
            ))
            
            animationDuration = 200
        }
        
        // Setup selection listener with animations
        bottomNavigationView.setOnItemSelectedListener { item ->
            animateSelection()
            true
        }
    }

    fun setMenu(@MenuRes menuRes: Int) {
        bottomNavigationView.inflateMenu(menuRes)
    }
    
    private fun animateSelection() {
        selectionAnimator.cancel()
        
        // Animate highlight angle for visual feedback
        val targetAngle = currentHighlightAngle + 0.5f
        if (targetAngle > 4f) {
            currentHighlightAngle = 2f
        }
        
        selectionAnimator.setFloatValues(currentHighlightAngle, targetAngle)
        selectionAnimator.addUpdateListener { animation ->
            currentHighlightAngle = animation.animatedValue as Float
            liquidGlassView.setHighlight(currentHighlightAngle, 1.5f, HighlightType.SPECULAR)
        }
        selectionAnimator.start()
        
        // Pulse effect
        liquidGlassView.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(150)
            .withEndAction {
                liquidGlassView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        selectionAnimator.cancel()
    }
}
