package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.MenuRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.LiquidGlassView

class GlassBottomTabs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val liquidGlassView: LiquidGlassView
    val bottomNavigationView: BottomNavigationView

    init {
        inflate(context, R.layout.glass_bottom_tabs, this)
        liquidGlassView = findViewById(R.id.tabs_background)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        liquidGlassView.setCornerRadii(
            topLeft = 24f * resources.displayMetrics.density,
            topRight = 24f * resources.displayMetrics.density,
            bottomRight = 0f,
            bottomLeft = 0f
        )
        liquidGlassView.setRefraction(
            height = 16f * resources.displayMetrics.density,
            amount = 24f * resources.displayMetrics.density,
            depthEffect = 0.1f
        )
    }

    fun setMenu(@MenuRes menuRes: Int) {
        bottomNavigationView.inflateMenu(menuRes)
    }
}
