package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.Effect

/**
 * A container that applies a "liquid glass" effect to its background.
 * It is a [FrameLayout] that internally manages a [GlassView] to create the effect,
 * allowing other views to be placed on top of the glass.
 */
class GlassContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val glassView: GlassView

    init {
        // Create the background GlassView, passing the attributes down to it.
        // This allows XML attributes like `blurRadius` and `cornerRadius` to be applied correctly.
        glassView = GlassView(context, attrs, defStyle)
        addView(glassView, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    // --- Public API Delegation ---
    // Expose methods from the internal GlassView so they can be called on the container.

    fun addContentEffect(effect: Effect) {
        glassView.addContentEffect(effect)
    }

    fun addEffect(effect: Effect) {
        glassView.addEffect(effect)
    }

    var cornerRadius: Float
        get() = glassView.cornerRadius
        set(value) {
            glassView.cornerRadius = value
        }

    var blurRadius: Float
        get() = glassView.blurRadius
        set(value) {
            glassView.blurRadius = value
        }

    var saturation: Float
        get() = glassView.saturation
        set(value) {
            glassView.saturation = value
        }

    var brightness: Float
        get() = glassView.brightness
        set(value) {
            glassView.brightness = value
        }

    var refractionIntensity: Float
        get() = glassView.refractionIntensity
        set(value) {
            glassView.refractionIntensity = value
        }
}
