package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.slider.Slider
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.HighlightType
import com.kyant.backdrop.xml.LiquidGlassView

class GlassSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val liquidGlassView: LiquidGlassView
    val slider: Slider

    init {
        // Read orientation from attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.GlassSlider)
        val orientation = a.getInt(R.styleable.GlassSlider_android_orientation, VERTICAL)
        a.recycle()

        this.orientation = orientation
        inflate(context, R.layout.glass_slider, this)
        liquidGlassView = findViewById(R.id.slider_track)
        slider = findViewById(R.id.slider)

        if (orientation == VERTICAL) {
            slider.rotation = 270f
        }

        liquidGlassView.setCornerRadius(16f * resources.displayMetrics.density)
        liquidGlassView.setRefraction(
            height = 8f * resources.displayMetrics.density,
            amount = 12f * resources.displayMetrics.density,
            depthEffect = 0.1f
        )
        liquidGlassView.animationDuration = 0 // Disable animation for instant response

        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                // Map slider value (0-100) to highlight angle (e.g., 1.5 to 4.5 radians)
                val angle = 1.5f + (value / 100f) * 3.0f
                liquidGlassView.setHighlight(angle, 1.0f, HighlightType.SPECULAR)
            }
        }
        // Set initial highlight
        val initialAngle = 1.5f + (slider.value / 100f) * 3.0f
        liquidGlassView.setHighlight(initialAngle, 1.0f, HighlightType.SPECULAR)
    }
}