package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class LiquidBottomTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    var isTabSelected: Boolean = false
        set(value) {
            field = value
            alpha = if (value) 1f else 0.7f
        }

    init {
        isClickable = true
        gravity = Gravity.CENTER
        setPadding(16, 16, 16, 16)
        setTextColor(Color.WHITE)
    }
}
