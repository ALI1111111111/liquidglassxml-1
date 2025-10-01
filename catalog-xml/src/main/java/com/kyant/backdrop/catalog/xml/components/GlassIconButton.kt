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
        
        // Add shadow for depth (matching Compose)
        val density = resources.displayMetrics.density
        setShadow(com.kyant.backdrop.xml.DefaultShadow(
            elevation = 4f * density,
            color = 0x0D000000.toInt(), // 5% black
            offsetX = 0f,
            offsetY = 2f * density
        ))
        
        // Add inner shadow for depth (matching Compose)
        setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
            elevation = 8f * density,
            color = 0x1A000000.toInt(), // 10% black
            offsetX = 0f,
            offsetY = 0f
        ))
        
        // ALWAYS draw surface - light frosted glass appearance
        onDrawSurface = { canvas ->
            // Light opacity for frosted glass effect
            val surfaceAlpha = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                26 // 10% on modern devices (blur + highlight + inner shadow create the effect)
            } else {
                38 // 15% on older devices (no blur, needs slightly more)
            }
            
            val surfacePaint = android.graphics.Paint().apply {
                color = Color.WHITE
                alpha = surfaceAlpha
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), surfacePaint)
        }

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
