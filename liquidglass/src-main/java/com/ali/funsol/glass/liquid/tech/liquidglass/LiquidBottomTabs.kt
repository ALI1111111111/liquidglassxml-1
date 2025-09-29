package com.example.liquidbottomtabs

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.widget.LinearLayoutCompat

class LiquidBottomTabs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val highlightView: GlassView

    init {
        orientation = HORIZONTAL
        // Pass the AttributeSet down to the child GlassView
        highlightView = GlassView(context, attrs, defStyle).apply {
            layoutParams = LayoutParams(0, 0)
            // Defaults can be set here, but XML attributes will override them
            cornerRadius = 999f
            blurRadius = 15f
        }
        addView(highlightView, 0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // Layout logic for the highlightView and other children
    }

    // Other methods and inner classes
}