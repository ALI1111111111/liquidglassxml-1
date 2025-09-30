package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.LiquidGlassView

class GlassButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    private val button: MaterialButton

    var text: CharSequence?
        get() = button.text
        set(value) {
            button.text = value
        }

    @ColorInt
    private var tintColor: Int = Color.TRANSPARENT
    @ColorInt
    private var surfaceColor: Int = Color.TRANSPARENT

    init {
        // Inflate the button layout and attach it to this view
        LayoutInflater.from(context).inflate(R.layout.glass_button_internal, this, true)
        button = findViewById(R.id.internal_button)

        // Apply custom attributes
        context.withStyledAttributes(attrs, R.styleable.GlassButton) {
            text = getString(R.styleable.GlassButton_android_text)
            tintColor = getColor(R.styleable.GlassButton_tint, Color.TRANSPARENT)
            surfaceColor = getColor(R.styleable.GlassButton_surfaceColor, Color.TRANSPARENT)
        }

        // Set default glass properties
        animationDuration = 200 // Enable animations
        setCornerRadius(24f * resources.displayMetrics.density)
        setRefraction(
            height = 16f * resources.displayMetrics.density,
            amount = 24f * resources.displayMetrics.density,
            depthEffect = 0.1f
        )
        setHighlight(
            angle = 2.5f,
            falloff = 1.5f,
            type = com.kyant.backdrop.xml.HighlightType.SPECULAR
        )

        if (tintColor != Color.TRANSPARENT) {
            button.setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(tintColor)
        }

        if (surfaceColor != Color.TRANSPARENT) {
            foreground = ColorDrawable(surfaceColor)
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Animate "pop" effect on press
                    setRefraction(
                        height = 24f * resources.displayMetrics.density,
                        amount = 32f * resources.displayMetrics.density,
                        depthEffect = 0.2f
                    )
                    setHighlight(
                        angle = 2.5f,
                        falloff = 0.5f, // Sharpen highlight
                        type = com.kyant.backdrop.xml.HighlightType.SPECULAR
                    )
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Animate back to idle state on release
                    setRefraction(
                        height = 16f * resources.displayMetrics.density,
                        amount = 24f * resources.displayMetrics.density,
                        depthEffect = 0.1f
                    )
                    setHighlight(
                        angle = 2.5f,
                        falloff = 1.5f, // Soften highlight
                        type = com.kyant.backdrop.xml.HighlightType.SPECULAR
                    )
                }
            }
            false // Allow click listener to proceed
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        button.setOnClickListener(l)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        button.isEnabled = enabled
    }
}
