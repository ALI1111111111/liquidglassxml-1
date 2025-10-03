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

package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.kyant.backdrop.xml.backdrop.XmlBackdrop
import com.kyant.backdrop.xml.backdrop.DrawableXmlBackdrop
import com.kyant.backdrop.xml.backdrop.CanvasXmlBackdrop

/**
 * A backdrop provider that captures content for liquid glass effects.
 * This is equivalent to LayerBackdrop in the Compose version.
 * 
 * It provides a backdrop for child glass views by capturing the background content
 * in the specific area where each glass view is positioned.
 */
class LayerBackdropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundDrawable: android.graphics.drawable.Drawable? = null
    private var backdropBitmap: Bitmap? = null
    private var backdropCanvas: Canvas? = null
    private val registeredGlassViews = mutableSetOf<View>()
    
    init {
        setWillNotDraw(false)
        // Make this view clip children so content renders properly
        clipChildren = true
        clipToPadding = true
    }
    
    /**
     * Sets the background content that will be captured for glass effects
     */
    fun setBackgroundDrawable(@DrawableRes drawableRes: Int) {
        backgroundDrawable = context.getDrawable(drawableRes)
        invalidate()
    }
    
    /**
     * Sets the background content that will be captured for glass effects
     */
    override fun setBackgroundDrawable(drawable: android.graphics.drawable.Drawable?) {
        backgroundDrawable = drawable
        invalidate()
    }
    
    /**
     * Register a glass view to receive backdrop updates
     */
    fun registerGlassView(view: View) {
        registeredGlassViews.add(view)
        updateGlassViewBackdrop(view)
    }
    
    /**
     * Unregister a glass view
     */
    fun unregisterGlassView(view: View) {
        registeredGlassViews.remove(view)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Create backdrop bitmap
        if (w > 0 && h > 0) {
            backdropBitmap?.recycle()
            backdropBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            backdropCanvas = Canvas(backdropBitmap!!)
        }
        
        // Update all registered glass views
        registeredGlassViews.forEach { updateGlassViewBackdrop(it) }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the background content
        drawBackgroundContent(canvas)
        
        // Update backdrop for all registered glass views
        registeredGlassViews.forEach { updateGlassViewBackdrop(it) }
    }
    
    private fun drawBackgroundContent(canvas: Canvas) {
        val drawable = backgroundDrawable ?: return
        
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        
        // Also capture to backdrop bitmap for glass views
        val backdropCanvas = backdropCanvas ?: return
        backdropCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawable.draw(backdropCanvas)
    }
    
    private fun updateGlassViewBackdrop(glassView: View) {
        val backdropBitmap = backdropBitmap ?: return
        
        // Get the position of the glass view relative to this backdrop view
        val viewLocation = IntArray(2)
        val backdropLocation = IntArray(2)
        
        glassView.getLocationInWindow(viewLocation)
        this.getLocationInWindow(backdropLocation)
        
        val relativeX = viewLocation[0] - backdropLocation[0]
        val relativeY = viewLocation[1] - backdropLocation[1]
        
        // Create area-specific backdrop
        val areaBackdrop = getBackdropForArea(relativeX, relativeY, glassView.width, glassView.height)
        
        // Apply this backdrop to the glass view
        when (glassView) {
            is com.kyant.backdrop.xml.views.LiquidGlassView -> {
                glassView.setBackgroundSource(areaBackdrop)
            }
            is com.kyant.backdrop.xml.views.LiquidGlassContainer -> {
                glassView.setBackgroundSource(areaBackdrop)
            }
        }
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Layout all child views normally
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(0, 0, width, height)
        }
        
        // Update backdrop for all glass views after layout
        if (changed) {
            registeredGlassViews.forEach { updateGlassViewBackdrop(it) }
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // Measure all children to fill the parent
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
            )
        }
    }
    
    /**
     * Gets a backdrop for the specified area
     */
    fun getBackdropForArea(x: Int, y: Int, width: Int, height: Int): XmlBackdrop {
        val backdropBitmap = backdropBitmap ?: return com.kyant.backdrop.xml.backdrop.EmptyXmlBackdrop
        
        if (x >= 0 && y >= 0 && x + width <= backdropBitmap.width && y + height <= backdropBitmap.height) {
            val croppedBitmap = Bitmap.createBitmap(backdropBitmap, x, y, width, height)
            return CanvasXmlBackdrop { canvas, w, h ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.isFilterBitmap = true
                canvas.drawBitmap(croppedBitmap, 0f, 0f, paint)
            }
        }
        
        return com.kyant.backdrop.xml.backdrop.EmptyXmlBackdrop
    }
}