package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class LiquidBottomTabs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val highlightView: GlassView
    private var selectedTabIndex = 0

    // --- Physics-based Animation for the highlight ---
    private val highlightXProperty = object : FloatPropertyCompat<GlassView>("x") {
        override fun getValue(view: GlassView): Float = view.x
        override fun setValue(view: GlassView, value: Float) {
            view.x = value
        }
    }

    private val highlightSpring: SpringAnimation by lazy {
        SpringAnimation(highlightView, highlightXProperty).setSpring(
            SpringForce().apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        )
    }

    init {
        orientation = HORIZONTAL
        highlightView = GlassView(context).apply {
            layoutParams = LayoutParams(0, 0)
            cornerRadius = 999f // Make it a circle
            blurRadius = 15f
        }
        addView(highlightView, 0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (childCount > 1) {
            val firstTab = children.elementAt(1)
            highlightView.layoutParams.width = firstTab.width
            highlightView.layoutParams.height = firstTab.height
            highlightView.requestLayout()
            updateHighlightPosition(false)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        for ((index, child) in children.withIndex()) {
            if (child is LiquidBottomTab) {
                child.setOnClickListener {
                    selectTab(index - 1) // Adjust for highlight view
                }
            }
        }
        selectTab(selectedTabIndex, false)
    }

    fun selectTab(index: Int, animate: Boolean = true) {
        if (index < 0 || index >= childCount - 1) return
        selectedTabIndex = index
        for ((i, child) in children.withIndex()) {
            if (child is LiquidBottomTab) {
                child.isTabSelected = (i - 1 == index)
            }
        }
        updateHighlightPosition(animate)
    }

    private fun updateHighlightPosition(animate: Boolean) {
        val selectedTab = children.elementAtOrNull(selectedTabIndex + 1) ?: return
        val targetX = selectedTab.left.toFloat()

        if (animate) {
            highlightSpring.animateToFinalPosition(targetX)
        } else {
            highlightView.x = targetX
        }
    }
}
