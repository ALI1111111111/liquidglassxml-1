package com.kyant.backdrop.xml.backdrop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.graphics.createBitmap

/**
 * A ViewGroup that captures its content to a bitmap layer that can be used as a backdrop.
 * This is the XML equivalent of Compose's LayerBackdrop.
 * 
 * Usage:
 * 1. Wrap your background content in this view
 * 2. Get the backdrop via getBackdrop()
 * 3. Pass it to LiquidGlassView.setBackgroundSource()
 * 
 * The view automatically updates the backdrop when:
 * - The view hierarchy changes
 * - Child views are laid out
 * - Content is invalidated
 */
class LayerBackdropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // Off-screen bitmap for capturing content
    internal var layerBitmap: Bitmap? = null
    private var layerCanvas: Canvas? = null
    
    // Backdrop interface that references this view's layer
    private val backdrop: LayerXmlBackdrop = LayerXmlBackdrop(this)
    
    // Track if we need to update the layer
    private var isDirty = true
    
    // Global position cache for coordinate calculations
    private val globalPosition = IntArray(2)
    
    init {
        setWillNotDraw(false)
        
        // Listen for layout changes
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                markDirty()
            }
        })
        
        // Listen for pre-draw to update layer before rendering
        viewTreeObserver.addOnPreDrawListener {
            if (isDirty) {
                updateLayer()
                isDirty = false
            }
            true
        }
    }
    
    /**
     * Get the backdrop that can be used with LiquidGlassView
     */
    fun getBackdrop(): XmlBackdrop = backdrop
    
    /**
     * Mark the layer as needing an update
     */
    fun markDirty() {
        isDirty = true
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        if (w > 0 && h > 0) {
            // Create new bitmap for the layer
            layerBitmap?.recycle()
            layerBitmap = createBitmap(w, h)
            layerCanvas = Canvas(layerBitmap!!)
            markDirty()
        }
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Layout children
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                child.layout(0, 0, r - l, b - t)
            }
        }
        
        if (changed) {
            markDirty()
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxWidth = 0
        var maxHeight = 0
        
        // Measure children
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight)
            }
        }
        
        // Apply padding
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom
        
        // Respect constraints
        maxWidth = maxWidth.coerceAtLeast(suggestedMinimumWidth)
        maxHeight = maxHeight.coerceAtLeast(suggestedMinimumHeight)
        
        setMeasuredDimension(
            resolveSize(maxWidth, widthMeasureSpec),
            resolveSize(maxHeight, heightMeasureSpec)
        )
    }
    
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        
        // Update layer if needed
        if (isDirty) {
            updateLayer()
            isDirty = false
        }
    }
    
    /**
     * Updates the off-screen layer by rendering all children
     */
    private fun updateLayer() {
        val bitmap = layerBitmap ?: return
        val canvas = layerCanvas ?: return
        
        // Clear the layer
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        
        // Draw all children to the layer
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                canvas.save()
                canvas.translate(child.left.toFloat(), child.top.toFloat())
                child.draw(canvas)
                canvas.restore()
            }
        }
        
        canvas.restore()
    }
    
    /**
     * Force update the layer immediately
     */
    fun updateLayerNow() {
        updateLayer()
        isDirty = false
    }
    
    /**
     * Get the current global position of this view
     */
    fun getGlobalPosition(): IntArray {
        getLocationOnScreen(globalPosition)
        return globalPosition
    }
    
    override fun invalidateDrawable(drawable: android.graphics.drawable.Drawable) {
        super.invalidateDrawable(drawable)
        markDirty()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        
        // Clean up resources
        layerBitmap?.recycle()
        layerBitmap = null
        layerCanvas = null
    }
}

/**
 * XmlBackdrop implementation that reads from a LayerBackdropView's bitmap layer
 * and handles coordinate translation.
 */
class LayerXmlBackdrop internal constructor(
    private val layerView: LayerBackdropView
) : XmlBackdrop {
    
    override val isCoordinatesDependent: Boolean = true
    
    /**
     * Draws the layer backdrop with coordinate translation
     */
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        val bitmap = layerView.layerBitmap ?: return
        
        // Get global positions
        val layerPos = layerView.getGlobalPosition()
        
        // Draw the bitmap without any translation for now
        // The translation will be handled by the calling code that knows the glass view position
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
    
    /**
     * Draws the layer backdrop with explicit coordinate translation for a glass view
     */
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float, glassViewX: Int, glassViewY: Int) {
        val bitmap = layerView.layerBitmap ?: return
        
        // Calculate offset between layer view and glass view
        val layerPos = layerView.getGlobalPosition()
        val offsetX = layerPos[0] - glassViewX
        val offsetY = layerPos[1] - glassViewY
        
        // Draw the bitmap translated to align with glass view position
        canvas.save()
        canvas.translate(offsetX.toFloat(), offsetY.toFloat())
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
    }
    
    /**
     * Get the layer view for direct access
     */
    fun getLayerView(): LayerBackdropView = layerView
    
    /**
     * Check if the layer is ready to be drawn
     */
    fun isReady(): Boolean = layerView.layerBitmap != null
}
