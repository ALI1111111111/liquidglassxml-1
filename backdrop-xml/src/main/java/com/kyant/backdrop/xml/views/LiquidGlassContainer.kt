/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyant.backdrop.xml.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.presets.LiquidGlassPresets

/**
 * A ViewGroup that applies liquid glass effects to its background while displaying children normally.
 * This container captures the background behind it and applies glass effects,
 * similar to the LayerBackdrop functionality in the Compose version.
 */
class LiquidGlassContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val liquidGlassView = LiquidGlassView(context, attrs, defStyleAttr)
    private var backgroundSource: View? = null
    
    init {
        // Add the liquid glass view as a background layer
        addView(liquidGlassView, 0, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        
        // Make the container draw its children in the correct order
        setWillNotDraw(false)
        
        // Initialize from attributes
        initFromAttributes(context, attrs)
    }
    
    private fun initFromAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        
        val typedArray = context.obtainStyledAttributes(attrs, com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer)
        try {
            // Corner radius
            val cornerRadius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_cornerRadius, 0f)
            if (cornerRadius > 0f) {
                setCornerRadius(cornerRadius)
            } else {
                // Individual corner radii
                val topLeft = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_cornerRadiusTopLeft, 0f)
                val topRight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_cornerRadiusTopRight, 0f)
                val bottomRight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_cornerRadiusBottomRight, 0f)
                val bottomLeft = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_cornerRadiusBottomLeft, 0f)
                
                if (topLeft > 0f || topRight > 0f || bottomRight > 0f || bottomLeft > 0f) {
                    setCornerRadii(topLeft, topRight, bottomRight, bottomLeft)
                }
            }
            
            // Check for preset first
            val preset = typedArray.getInt(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_glassPreset, 0)
            if (preset != 0) {
                applyPreset(preset)
            } else {
                // Individual effect configurations
                
                // Refraction effect
                val refractionHeight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_refractionHeight, 0f)
                if (refractionHeight > 0f) {
                    val refractionAmount = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_refractionAmount, refractionHeight)
                    val hasDepthEffect = typedArray.getBoolean(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_refractionDepthEffect, false)
                    setRefractionEffect(RefractionEffect(refractionHeight, refractionAmount, hasDepthEffect))
                }
                
                // Dispersion effect
                val dispersionHeight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_dispersionHeight, 0f)
                if (dispersionHeight > 0f) {
                    val dispersionAmount = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_dispersionAmount, dispersionHeight)
                    setDispersionEffect(DispersionEffect(dispersionHeight, dispersionAmount))
                }
                
                // Blur effect
                val blurRadius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_blurRadius, 0f)
                if (blurRadius > 0f) {
                    val blurStyle = when (typedArray.getInt(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_blurStyle, 0)) {
                        1 -> BlurEffect.BlurStyle.SOLID
                        2 -> BlurEffect.BlurStyle.OUTER
                        3 -> BlurEffect.BlurStyle.INNER
                        else -> BlurEffect.BlurStyle.NORMAL
                    }
                    setBlurEffect(BlurEffect(blurRadius, blurStyle))
                }
                
                // Highlight effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_highlightAngle)) {
                    val angle = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_highlightAngle, 0f)
                    val falloff = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_highlightFalloff, 2f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_highlightColor, android.graphics.Color.WHITE)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_highlightAlpha, 0.3f)
                    setHighlightEffect(HighlightEffect(angle, falloff, color, alpha))
                }
                
                // Shadow effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowRadius)) {
                    val offsetX = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowOffsetX, 0f)
                    val offsetY = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowOffsetY, 0f)
                    val radius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowRadius, 0f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowColor, android.graphics.Color.BLACK)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_shadowAlpha, 0.25f)
                    setShadowEffect(ShadowEffect(offsetX, offsetY, radius, color, alpha))
                }
                
                // Inner shadow effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowRadius)) {
                    val offsetX = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowOffsetX, 0f)
                    val offsetY = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowOffsetY, 0f)
                    val radius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowRadius, 0f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowColor, android.graphics.Color.BLACK)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassContainer_innerShadowAlpha, 0.2f)
                    setInnerShadowEffect(InnerShadowEffect(offsetX, offsetY, radius, color, alpha))
                }
            }
        } finally {
            typedArray.recycle()
        }
    }
    
    private fun applyPreset(preset: Int) {
        val effectBuilder = when (preset) {
            1 -> LiquidGlassPresets.iosGlassButton(12f)
            2 -> LiquidGlassPresets.materialGlassCard(16f)
            3 -> LiquidGlassPresets.frostedGlassOverlay(8f)
            4 -> LiquidGlassPresets.chromaticLiquidGlass(20f)
            5 -> LiquidGlassPresets.subtleGlassPanel(12f)
            6 -> LiquidGlassPresets.gamingLiquidGlass(16f)
            7 -> LiquidGlassPresets.minimalistGlass(8f)
            8 -> LiquidGlassPresets.heavyLiquidGlass(24f)
            9 -> LiquidGlassPresets.notificationGlass(16f)
            10 -> LiquidGlassPresets.pressedGlassButton(12f)
            11 -> LiquidGlassPresets.glassNavigationBar(0f)
            12 -> LiquidGlassPresets.glassFAB(28f)
            13 -> LiquidGlassPresets.dialogGlassBackdrop(20f)
            else -> return
        }
        
        effectBuilder.applyTo(this)
    }
    
    /**
     * Sets the view that should be used as the background source for glass effects
     */
    fun setBackgroundSource(view: View?) {
        backgroundSource = view
        liquidGlassView.setBackgroundSource(view)
    }
    
    /**
     * Sets the drawable that should be used as the background source for glass effects
     */
    fun setBackgroundSource(drawable: android.graphics.drawable.Drawable?) {
        liquidGlassView.setBackgroundSource(drawable)
    }
    
    /**
     * Sets the XmlBackdrop that should be used as the background source for glass effects
     */
    fun setBackgroundSource(backdrop: com.kyant.backdrop.xml.backdrop.XmlBackdrop) {
        liquidGlassView.setBackgroundSource(backdrop)
    }
    
    /**
     * Configures the corner radius for the glass effect
     */
    fun setCornerRadius(radius: Float) {
        liquidGlassView.setCornerRadius(radius)
    }
    
    /**
     * Configures individual corner radii
     */
    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        liquidGlassView.setCornerRadii(topLeft, topRight, bottomRight, bottomLeft)
    }
    
    /**
     * Applies refraction effect to the background
     */
    fun setRefractionEffect(effect: RefractionEffect?) {
        liquidGlassView.setRefractionEffect(effect)
    }
    
    /**
     * Applies dispersion effect to the background
     */
    fun setDispersionEffect(effect: DispersionEffect?) {
        liquidGlassView.setDispersionEffect(effect)
    }
    
    /**
     * Applies blur effect to the background
     */
    fun setBlurEffect(effect: BlurEffect?) {
        liquidGlassView.setBlurEffect(effect)
    }
    
    /**
     * Applies highlight effect
     */
    fun setHighlightEffect(effect: HighlightEffect?) {
        liquidGlassView.setHighlightEffect(effect)
    }
    
    /**
     * Applies shadow effect
     */
    fun setShadowEffect(effect: ShadowEffect?) {
        liquidGlassView.setShadowEffect(effect)
    }
    
    /**
     * Applies inner shadow effect
     */
    fun setInnerShadowEffect(effect: InnerShadowEffect?) {
        liquidGlassView.setInnerShadowEffect(effect)
    }
    
    /**
     * Applies color filter effect
     */
    fun setColorFilterEffect(effect: ColorFilterEffect?) {
        liquidGlassView.setColorFilterEffect(effect)
    }
    
    /**
     * Applies gamma adjustment effect
     */
    fun setGammaAdjustmentEffect(effect: GammaAdjustmentEffect?) {
        liquidGlassView.setGammaAdjustmentEffect(effect)
    }
    
    /**
     * Applies exposure adjustment effect
     */
    fun setExposureAdjustmentEffect(effect: ExposureAdjustmentEffect?) {
        liquidGlassView.setExposureAdjustmentEffect(effect)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // Measure all children
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams
                
                if (child == liquidGlassView) {
                    // The liquid glass view should fill the entire container
                    child.layout(0, 0, r - l, b - t)
                } else {
                    // Layout other children normally
                    val childLeft = paddingLeft
                    val childTop = paddingTop
                    val childRight = childLeft + child.measuredWidth
                    val childBottom = childTop + child.measuredHeight
                    
                    child.layout(childLeft, childTop, childRight, childBottom)
                }
            }
        }
    }
    
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }
    
    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): LayoutParams {
        return LayoutParams(lp)
    }
    
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }
    
    /**
     * Layout parameters for LiquidGlassContainer children
     */
    class LayoutParams : MarginLayoutParams {
        
        constructor(width: Int, height: Int) : super(width, height)
        
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        
        constructor(source: MarginLayoutParams?) : super(source)
    }
}