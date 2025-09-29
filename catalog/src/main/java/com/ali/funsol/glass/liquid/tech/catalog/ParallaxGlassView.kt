package com.ali.funsol.glass.liquid.tech.catalog

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.ali.funsol.glass.liquid.tech.liquidglass.GlassView
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.RefractionEffect

class ParallaxGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle), ParallaxScrollView.OnScrollListener {

    private val refractionEffect = RefractionEffect(context)
    private var parallaxFactor = 0.2f
    private var scrollYPosition = 0f

    private val scrollYSpring: SpringAnimation = SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, 0f)
        .setSpring(SpringForce()
            .setStiffness(SpringForce.STIFFNESS_VERY_LOW)
            .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY))

    init {
        addEffect(refractionEffect)
    }

    override fun onScroll(scrollY: Int) {
        val targetY = scrollY * parallaxFactor
        scrollYSpring.animateToFinalPosition(targetY)

        val scrollDelta = scrollY - scrollYPosition
        scrollYPosition = scrollY.toFloat()

        // Increase refraction intensity based on scroll speed
        val intensity = (Math.abs(scrollDelta) / 100f).coerceIn(0f, 0.1f)
        refractionEffect.intensity = intensity
        refractionEffect.hasDepthEffect = true // Enable depth effect for more realism
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        refractionEffect.destroy()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
