package com.kyant.backdrop.xml.backdrop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
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
 * 
 * Performance optimizations:
 * - Real-time updates using postInvalidateOnAnimation for 60fps
 * - Pre-draw listener ensures layer is updated before frame rendering
 * - Dirty tracking to avoid unnecessary updates
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
    
    // Track if layer needs updating
    private var isDirty = true
    
    // Global position cache for coordinate calculations
    private val globalPosition = IntArray(2)
    
    // Background image support
    private var backgroundDrawable: Drawable? = null
    private var backgroundBitmap: Bitmap? = null
    
    // Blur effect support (matching Compose BlurEffect)
    private var blurRadius: Float = 0f
    private var dimAlpha: Float = 0f
    
    init {
        setWillNotDraw(false)
        
        // Listen for layout changes to mark dirty
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                isDirty = true
            }
        })
        
        // Critical: Use pre-draw listener to update layer BEFORE any frame renders
        // This matches Compose's behavior where graphicsLayer.record() is called every frame
        viewTreeObserver.addOnPreDrawListener {
            // Update the layer if dirty (content changed)
            if (isDirty) {
                updateLayerNow()
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
     * Mark layer as dirty and request update
     */
    fun invalidateLayer() {
        isDirty = true
        postInvalidateOnAnimation()
    }
    
    /**
     * Force immediate layer update
     * This is called by the pre-draw listener when layer is dirty
     */
    fun updateLayerNow() {
        val bitmap = layerBitmap ?: return
        val canvas = layerCanvas ?: return
        
        // Clear the layer
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        
        // Draw background image first if set
        backgroundBitmap?.let {
            canvas.drawBitmap(it, null, android.graphics.Rect(0, 0, width, height), null)
        } ?: backgroundDrawable?.let {
            it.setBounds(0, 0, width, height)
            it.draw(canvas)
        }
        
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
     * Set a background image for the layer
     */
    fun setBackgroundImage(drawable: Drawable?) {
        backgroundDrawable = drawable
        backgroundBitmap = null
        isDirty = true
        postInvalidateOnAnimation()
    }
    
    /**
     * Set a background image from a bitmap
     */
    fun setBackgroundImage(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        backgroundDrawable = null
        isDirty = true
        postInvalidateOnAnimation()
    }
    
    /**
     * Set backdrop blur effect (matching Compose BlurEffect)
     * @param radius Blur radius in pixels (0f to disable)
     * @param dimAlpha Dim overlay alpha (0f to 1f)
     */
    fun setBackdropBlur(radius: Float, dimAlpha: Float = 0f) {
        this.blurRadius = radius
        this.dimAlpha = dimAlpha
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (radius > 0f) {
                setRenderEffect(
                    android.graphics.RenderEffect.createBlurEffect(
                        radius,
                        radius,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                )
            } else {
                setRenderEffect(null)
            }
        }
        
        // Apply dim overlay
        if (dimAlpha > 0f) {
            setBackgroundColor(Color.argb((dimAlpha * 255).toInt().coerceIn(0, 255), 0, 0, 0))
        } else {
            setBackgroundColor(Color.TRANSPARENT)
        }
        
        invalidate()
    }
    
    /**
     * Alias for updateLayerNow() - kept for compatibility
     */
    fun updateNow() = updateLayerNow()

    /**
     * Get the current global position of this view
     */
    fun getGlobalPosition(): IntArray {
        getLocationOnScreen(globalPosition)
        return globalPosition
    }

    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        if (w > 0 && h > 0) {
            // Create new bitmap for the layer
            layerBitmap?.recycle()
            layerBitmap = createBitmap(w, h)
            layerCanvas = Canvas(layerBitmap!!)
            isDirty = true
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
            isDirty = true
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
        // Layer is updated in pre-draw listener when dirty
        // Mark dirty after drawing children for next frame
        isDirty = true
    }
    
    override fun invalidateDrawable(drawable: android.graphics.drawable.Drawable) {
        super.invalidateDrawable(drawable)
        isDirty = true
        postInvalidateOnAnimation()
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
