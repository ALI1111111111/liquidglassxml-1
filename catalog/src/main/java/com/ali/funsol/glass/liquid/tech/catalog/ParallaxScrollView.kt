package com.ali.funsol.glass.liquid.tech.catalog

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class ParallaxScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {

    interface OnScrollListener {
        fun onScroll(scrollY: Int)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is OnScrollListener) {
                (child as OnScrollListener).onScroll(t)
            }
        }
    }
}
