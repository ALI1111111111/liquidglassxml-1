package com.ali.funsol.glass.liquid.tech.catalog

import android.content.Context
import android.util.AttributeSet
import com.ali.funsol.glass.liquid.tech.liquidglass.GlassView

class ParallaxGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle), ParallaxScrollView.OnScrollListener {

    private var parallaxFactor = 0.5f

    override fun onScroll(scrollY: Int) {
        translationY = -scrollY * parallaxFactor
    }
}
