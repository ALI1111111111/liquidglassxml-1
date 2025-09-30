package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.LiquidGlassView

class GlassIconButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private var isToggled: Boolean = false

    var icon: Drawable?
        get() = imageView.drawable
        set(value) {
            imageView.setImageDrawable(value)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.glass_icon_button_internal, this, true)
        imageView = findViewById(R.id.internal_icon)

        context.withStyledAttributes(attrs, R.styleable.GlassIconButton) {
            icon = getDrawable(R.styleable.GlassIconButton_android_src)
        }

        // Make it circular
        setShapePath(android.graphics.Path().apply {
            addOval(0f, 0f, 64f * resources.displayMetrics.density, 64f * resources.displayMetrics.density, android.graphics.Path.Direction.CW)
        })

        setRefraction(
            height = 32f * resources.displayMetrics.density,
            amount = 48f * resources.displayMetrics.density,
            depthEffect = 0.2f
        )
        setHighlight(
            angle = 2.5f,
            falloff = 1.0f,
            type = com.kyant.backdrop.xml.HighlightType.SPECULAR
        )

        setOnClickListener {
            isToggled = !isToggled
            updateToggleState()
        }
        updateToggleState()
    }

    private fun updateToggleState() {
        if (isToggled) {
            backgroundTintList = ColorStateList.valueOf(0xFF0088FF.toInt())
            imageView.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            imageView.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }
    }
}
