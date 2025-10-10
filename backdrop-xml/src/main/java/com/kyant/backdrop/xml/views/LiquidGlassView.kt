package com.kyant.backdrop.xml.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.shaders.LiquidGlassShaders
import com.kyant.backdrop.xml.shaders.RuntimeShaderCacheScope
import com.kyant.backdrop.xml.shaders.RuntimeShaderCacheScopeImpl
import com.kyant.backdrop.xml.presets.LiquidGlassPresets
import com.kyant.backdrop.xml.backdrop.*
import kotlin.math.*
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation

/**
 * A custom View that provides liquid glass effects for XML layouts.
 * This is the main component that replicates the functionality of the Compose drawBackdrop modifier.
 */
class LiquidGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), RuntimeShaderCacheScope by RuntimeShaderCacheScopeImpl() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Bitmap for capturing background content
    private var backgroundBitmap: Bitmap? = null
    private var backgroundCanvas: Canvas? = null
    
    // Corner radius for rounded rectangle effects
    private var cornerRadiusTopLeft = 0f
    private var cornerRadiusTopRight = 0f
    private var cornerRadiusBottomRight = 0f
    private var cornerRadiusBottomLeft = 0f
    
    // Surface tint (drawn over backdrop but under content)
    // Default: TRANSPARENT - only add when explicitly set
    // Note: Compose uses onDrawSurface callback, not a default tint
    private var surfaceColor: Int = Color.TRANSPARENT
    
    // Effect configurations
    private var refractionEffect: RefractionEffect? = null
    private var dispersionEffect: DispersionEffect? = null
    private var blurEffect: BlurEffect? = null
    private var highlightEffect: HighlightEffect? = null
    private var shadowEffect: ShadowEffect? = null
    private var innerShadowEffect: InnerShadowEffect? = null
    private var colorFilterEffect: ColorFilterEffect? = null
    private var gammaAdjustmentEffect: GammaAdjustmentEffect? = null
    private var exposureAdjustmentEffect: ExposureAdjustmentEffect? = null
    
    // Background content source
    private var backgroundDrawable: Drawable? = null
    private var backgroundView: View? = null
    private var xmlBackdrop: XmlBackdrop = EmptyXmlBackdrop
    
    // Position tracking for dynamic backdrop updates
    private val globalPosition = IntArray(2)
    private var lastKnownX = 0
    private var lastKnownY = 0
    
    // Layout listener for position changes
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        updatePositionAndBackdrop()
    }
    
    init {
        initFromAttributes(context, attrs)
        setWillNotDraw(false)
        
        // Register layout listener
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }
    
    private fun initFromAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        
        val typedArray = context.obtainStyledAttributes(attrs, com.kyant.backdrop.xml.R.styleable.LiquidGlassView)
        
        try {
            // Corner radius
            val cornerRadius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_cornerRadius, 0f)
            if (cornerRadius > 0f) {
                setCornerRadius(cornerRadius)
            } else {
                // Individual corner radii
                val topLeft = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_cornerRadiusTopLeft, 0f)
                val topRight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_cornerRadiusTopRight, 0f)
                val bottomRight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_cornerRadiusBottomRight, 0f)
                val bottomLeft = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_cornerRadiusBottomLeft, 0f)
                
                if (topLeft > 0f || topRight > 0f || bottomRight > 0f || bottomLeft > 0f) {
                    setCornerRadii(topLeft, topRight, bottomRight, bottomLeft)
                }
            }
            
            // Check for preset first
            val preset = typedArray.getInt(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_glassPreset, 0)
            if (preset != 0) {
                applyPreset(preset)
            } else {
                // Individual effect configurations
                
                // Refraction effect
                val refractionHeight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_refractionHeight, 0f)
                if (refractionHeight > 0f) {
                    val refractionAmount = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_refractionAmount, refractionHeight)
                    val hasDepthEffect = typedArray.getBoolean(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_refractionDepthEffect, false)
                    setRefractionEffect(RefractionEffect(refractionHeight, refractionAmount, hasDepthEffect))
                }
                
                // Dispersion effect
                val dispersionHeight = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_dispersionHeight, 0f)
                if (dispersionHeight > 0f) {
                    val dispersionAmount = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_dispersionAmount, dispersionHeight)
                    setDispersionEffect(DispersionEffect(dispersionHeight, dispersionAmount))
                }
                
                // Blur effect
                val blurRadius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_blurRadius, 0f)
                if (blurRadius > 0f) {
                    val blurStyle = when (typedArray.getInt(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_blurStyle, 0)) {
                        1 -> BlurEffect.BlurStyle.SOLID
                        2 -> BlurEffect.BlurStyle.OUTER
                        3 -> BlurEffect.BlurStyle.INNER
                        else -> BlurEffect.BlurStyle.NORMAL
                    }
                    setBlurEffect(BlurEffect(blurRadius, blurStyle))
                }
                
                // Highlight effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_highlightAngle)) {
                    val angle = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_highlightAngle, 0f)
                    val falloff = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_highlightFalloff, 2f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_highlightColor, Color.WHITE)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_highlightAlpha, 0.3f)
                    setHighlightEffect(HighlightEffect(angle, falloff, color, alpha))
                }
                
                // Shadow effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowRadius)) {
                    val offsetX = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowOffsetX, 0f)
                    val offsetY = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowOffsetY, 0f)
                    val radius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowRadius, 0f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowColor, Color.BLACK)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_shadowAlpha, 0.25f)
                    setShadowEffect(ShadowEffect(offsetX, offsetY, radius, color, alpha))
                }
                
                // Inner shadow effect
                if (typedArray.hasValue(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowRadius)) {
                    val offsetX = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowOffsetX, 0f)
                    val offsetY = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowOffsetY, 0f)
                    val radius = typedArray.getDimension(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowRadius, 0f)
                    val color = typedArray.getColor(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowColor, Color.BLACK)
                    val alpha = typedArray.getFloat(com.kyant.backdrop.xml.R.styleable.LiquidGlassView_innerShadowAlpha, 0.2f)
                    setInnerShadowEffect(InnerShadowEffect(offsetX, offsetY, radius, color, alpha))
                }
            }
            
            // Apply DEFAULT vibrancy effect (saturation 1.5x)
            // This matches Compose version where vibrancy() is automatically applied
            // Users can override this by calling setColorFilterEffect() after initialization
            setColorFilterEffect(ColorFilterEffect.vibrant())
            
        } finally {
            typedArray.recycle()
        }
    }


    private fun applyPreset(preset: Int) {
        val effectBuilder = when (preset) {
            1 -> LiquidGlassPresets.iosGlassButton(cornerRadiusTopLeft.coerceAtLeast(12f))
            2 -> LiquidGlassPresets.materialGlassCard(cornerRadiusTopLeft.coerceAtLeast(16f))
            3 -> LiquidGlassPresets.frostedGlassOverlay(cornerRadiusTopLeft.coerceAtLeast(8f))
            4 -> LiquidGlassPresets.chromaticLiquidGlass(cornerRadiusTopLeft.coerceAtLeast(20f))
            5 -> LiquidGlassPresets.subtleGlassPanel(cornerRadiusTopLeft.coerceAtLeast(12f))
            6 -> LiquidGlassPresets.gamingLiquidGlass(cornerRadiusTopLeft.coerceAtLeast(16f))
            7 -> LiquidGlassPresets.minimalistGlass(cornerRadiusTopLeft.coerceAtLeast(8f))
            8 -> LiquidGlassPresets.heavyLiquidGlass(cornerRadiusTopLeft.coerceAtLeast(24f))
            9 -> LiquidGlassPresets.notificationGlass(cornerRadiusTopLeft.coerceAtLeast(16f))
            10 -> LiquidGlassPresets.pressedGlassButton(cornerRadiusTopLeft.coerceAtLeast(12f))
            11 -> LiquidGlassPresets.glassNavigationBar(0f)
            12 -> LiquidGlassPresets.glassFAB(28f)
            13 -> LiquidGlassPresets.dialogGlassBackdrop(cornerRadiusTopLeft.coerceAtLeast(20f))
            else -> return
        }
        
        effectBuilder.applyTo(this)
    }

    /**
     * Sets the corner radius for all corners
     */
    fun setCornerRadius(radius: Float) {
        cornerRadiusTopLeft = radius
        cornerRadiusTopRight = radius
        cornerRadiusBottomRight = radius
        cornerRadiusBottomLeft = radius
        invalidate()
    }
    
    /**
     * Sets individual corner radii
     */
    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        cornerRadiusTopLeft = topLeft
        cornerRadiusTopRight = topRight
        cornerRadiusBottomRight = bottomRight
        cornerRadiusBottomLeft = bottomLeft
        invalidate()
    }
    
    /**
     * Configures refraction effect
     */
    fun setRefractionEffect(effect: RefractionEffect?) {
        refractionEffect = effect
        invalidate()
    }
    
    /**
     * Configures dispersion effect
     */
    fun setDispersionEffect(effect: DispersionEffect?) {
        dispersionEffect = effect
        invalidate()
    }
    
    /**
     * Configures blur effect
     */
    fun setBlurEffect(effect: BlurEffect?) {
        blurEffect = effect
        updateBlurEffect()
        invalidate()
    }
    
    /**
     * Configures highlight effect
     */
    fun setHighlightEffect(effect: HighlightEffect?) {
        highlightEffect = effect
        updateHighlightEffect()
        invalidate()
    }
    
    /**
     * Configures shadow effect
     */
    fun setShadowEffect(effect: ShadowEffect?) {
        shadowEffect = effect
        updateShadowEffect()
        invalidate()
    }
    
    /**
     * Configures inner shadow effect
     */
    fun setInnerShadowEffect(effect: InnerShadowEffect?) {
        innerShadowEffect = effect
        invalidate()
    }
    
    /**
     * Configures color filter effect
     */
    fun setColorFilterEffect(effect: ColorFilterEffect?) {
        colorFilterEffect = effect
        invalidate()
    }
    
    /**
     * Sets the surface tint color drawn over the backdrop.
     * Matches Compose's onDrawSurface parameter.
     * @param color ARGB color value (e.g., Color.argb(13, 0, 0, 0) for Black 5%)
     */
    fun setSurfaceColor(color: Int) {
        surfaceColor = color
        invalidate()
    }
    
    /**
     * Configures gamma adjustment effect
     */
    fun setGammaAdjustmentEffect(effect: GammaAdjustmentEffect?) {
        gammaAdjustmentEffect = effect
        invalidate()
    }
    
    /**
     * Configures exposure adjustment effect
     */
    fun setExposureAdjustmentEffect(effect: ExposureAdjustmentEffect?) {
        exposureAdjustmentEffect = effect
        invalidate()
    }
    
    /**
     * Sets the background content source as a Drawable
     */
    fun setBackgroundSource(drawable: Drawable?) {
        backgroundDrawable = drawable
        backgroundView = null
        xmlBackdrop = if (drawable != null) {
            DrawableXmlBackdrop(drawable)
        } else {
            EmptyXmlBackdrop
        }
        invalidate()
    }
    
    /**
     * Sets the background content source as another View
     */
    fun setBackgroundSource(view: View?) {
        backgroundView = view
        backgroundDrawable = null
        xmlBackdrop = if (view != null) {
            ViewXmlBackdrop(view)
        } else {
            EmptyXmlBackdrop
        }
        invalidate()
    }
    
    /**
     * Sets a custom XmlBackdrop as the background source
     */
    fun setBackgroundSource(backdrop: XmlBackdrop) {
        xmlBackdrop = backdrop
        backgroundDrawable = null
        backgroundView = null
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Create background bitmap for capturing content
        if (w > 0 && h > 0) {
            backgroundBitmap?.recycle()
            backgroundBitmap = createBitmap(w, h)
            backgroundCanvas = Canvas(backgroundBitmap!!)
        }
        
        // Update position tracking
        updatePositionAndBackdrop()
    }
    
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        
        if (changed) {
            // Position might have changed, update backdrop
            updatePositionAndBackdrop()
        }
    }
    
    /**
     * Updates global position tracking and refreshes backdrop if position changed
     * Optimized for real-time performance
     */
    private fun updatePositionAndBackdrop() {
        getLocationOnScreen(globalPosition)
        
        val currentX = globalPosition[0]
        val currentY = globalPosition[1]
        
        // Check if position actually changed
        if (currentX != lastKnownX || currentY != lastKnownY) {
            lastKnownX = currentX
            lastKnownY = currentY
            
            // Backdrop needs to be recaptured with new position
            // Use postInvalidateOnAnimation for smooth 60fps updates
            if (xmlBackdrop.isCoordinatesDependent) {
                postInvalidateOnAnimation()
            }
        }
    }
    
    override fun invalidate() {
        super.invalidate()
        // Also invalidate the backdrop layer to ensure fresh content
        if (xmlBackdrop is LayerXmlBackdrop) {
            val layerBackdrop = xmlBackdrop as LayerXmlBackdrop
            layerBackdrop.getLayerView().invalidateLayer()
        }
    }
    
    override fun postInvalidate() {
        super.postInvalidate()
        // Also invalidate the backdrop layer to ensure fresh content
        if (xmlBackdrop is LayerXmlBackdrop) {
            val layerBackdrop = xmlBackdrop as LayerXmlBackdrop
            layerBackdrop.getLayerView().invalidateLayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // Capture backdrop - layer is already updated by pre-draw listener
        captureBackgroundContent()

        // Draw main glass effect with backdrop
        val clipPath = createRoundedRectPath(w, h)
        canvas.save()
        canvas.clipPath(clipPath)
        
        val bgBitmap = backgroundBitmap
        if (bgBitmap != null) {
            // Draw glass effect with refraction and blur shaders
            drawGlassEffect(canvas, w, h)
        }
        
        canvas.restore()
        
        // Draw shadow OUTSIDE the clipped region (if set)
        if (shadowEffect != null) {
            drawShadowEffect(canvas, w, h)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // Draw effects AFTER children are drawn
        val w = width.toFloat()
        val h = height.toFloat()
        
        val clipPath = createRoundedRectPath(w, h)
        canvas.save()
        canvas.clipPath(clipPath)
        
        // Draw inner shadow if set
        if (innerShadowEffect != null) {
            drawInnerShadowEffect(canvas, w, h)
        }
        
        // Draw highlight if set
        if (highlightEffect != null) {
            drawHighlightEffect(canvas, w, h)
        }
        
        canvas.restore()
    }


    private fun captureBackgroundContent() {
        val bgCanvas = backgroundCanvas ?: return
        val bgBitmap = backgroundBitmap ?: return

        // Clear the background bitmap
        bgCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Draw background using XmlBackdrop with coordinate information
        // Layer is already fresh from pre-draw listener (matching Compose behavior)
        if (xmlBackdrop.isCoordinatesDependent) {
            // Update position before drawing
            getLocationOnScreen(globalPosition)
            xmlBackdrop.drawBackdrop(bgCanvas, width.toFloat(), height.toFloat(), globalPosition[0], globalPosition[1])
        } else {
            // Simple backdrop without coordinates
            xmlBackdrop.drawBackdrop(bgCanvas, width.toFloat(), height.toFloat())
        }
    }



    private fun drawShadowEffect(canvas: Canvas, width: Float, height: Float) {
        val shadow = shadowEffect ?: return
        val path = createRoundedRectPath(width, height)

        shadowPaint.apply {
            color = shadow.color
            alpha = (shadow.alpha * 255).toInt()
            maskFilter = BlurMaskFilter(shadow.radius, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.withTranslation(shadow.offsetX, shadow.offsetY) {
            drawPath(path, shadowPaint)
        }
    }


    /**
     * Main glass effect drawing - applies refraction shader and blur effect
     * Matches Compose drawBackdrop behavior without extra surface layers
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun drawGlassEffect(canvas: Canvas, width: Float, height: Float) {
        val bgBitmap = backgroundBitmap ?: return
        
        // Create base bitmap shader
        val bitmapShader = BitmapShader(bgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        
        // Apply refraction shader if enabled
        val refraction = refractionEffect
        val finalShader = if (refraction != null && refraction.height > 0f) {
            val shader = obtainRuntimeShader("Refraction", LiquidGlassShaders.REFRACTION_SHADER)
            shader.setFloatUniform("size", width, height)
            shader.setFloatUniform("cornerRadii", getCornerRadiiArray())
            shader.setFloatUniform("refractionHeight", refraction.height)
            shader.setFloatUniform("refractionAmount", refraction.amount)
            shader.setFloatUniform("depthEffect", if (refraction.hasDepthEffect) 1f else 0f)
            shader.setInputShader("content", bitmapShader)
            shader
        } else {
            bitmapShader
        }

        paint.shader = finalShader
        
        // Apply color filter (vibrancy) if set
        colorFilterEffect?.let {
            paint.colorFilter = createColorMatrixColorFilter(it)
        }
        
        // Draw the glass effect
        canvas.drawRect(0f, 0f, width, height, paint)
        
        // Reset for next draw
        paint.shader = null
        paint.colorFilter = null
    }

    
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun drawAdvancedGlassEffect(canvas: Canvas, bgBitmap: Bitmap, width: Float, height: Float) {
        val bitmapShader = BitmapShader(bgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        
        // Apply refraction effect
        val refractionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val refraction = refractionEffect
        if (refraction != null && refraction.height > 0f) {
            val shader = obtainRuntimeShader("Refraction", LiquidGlassShaders.REFRACTION_SHADER)
            shader.setFloatUniform("size", width, height)
            shader.setFloatUniform("cornerRadii", getCornerRadiiArray())
            shader.setFloatUniform("refractionHeight", refraction.height)
            shader.setFloatUniform("refractionAmount", refraction.amount)
            shader.setFloatUniform("depthEffect", if (refraction.hasDepthEffect) 1f else 0f)
            shader.setInputShader("content", bitmapShader)
            Log.d("Advance", " Advance on height: ${refraction.height}, amount: ${refraction.amount}")

            refractionPaint.shader = shader
        } else {
            refractionPaint.shader = bitmapShader
        }
        
        canvas.drawRect(0f, 0f, width, height, refractionPaint)
        
        // Apply color filter effect
        val colorFilter = colorFilterEffect
        if (colorFilter != null) {
            val colorFilterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            colorFilterPaint.colorFilter = createColorMatrixColorFilter(colorFilter)
            colorFilterPaint.shader = bitmapShader
            canvas.drawRect(0f, 0f, width, height, colorFilterPaint)
        }
        
        // Apply gamma adjustment effect
        val gamma = gammaAdjustmentEffect
        if (gamma != null && gamma.power != 1f) {
            val gammaShader = obtainRuntimeShader("GammaAdjustment", LiquidGlassShaders.GAMMA_ADJUSTMENT_SHADER)
            gammaShader.setFloatUniform("power", gamma.power)
            gammaShader.setInputShader("content", bitmapShader)
            
            val gammaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            gammaPaint.shader = gammaShader
            canvas.drawRect(0f, 0f, width, height, gammaPaint)
        }
        
        // Apply exposure adjustment effect
        val exposure = exposureAdjustmentEffect
        if (exposure != null && exposure.ev != 0f) {
            val exposurePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            exposurePaint.colorFilter = createExposureColorFilter(exposure)
            exposurePaint.shader = bitmapShader
            canvas.drawRect(0f, 0f, width, height, exposurePaint)
        }
        
        // Apply dispersion effect
        val dispersion = dispersionEffect
        if (dispersion != null && dispersion.height > 0f) {
            val dispersionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val shader = obtainRuntimeShader("Dispersion", LiquidGlassShaders.DISPERSION_SHADER)
            shader.setFloatUniform("size", width, height)
            shader.setFloatUniform("cornerRadii", getCornerRadiiArray())
            shader.setFloatUniform("dispersionHeight", dispersion.height)
            shader.setFloatUniform("dispersionAmount", dispersion.amount)
            shader.setInputShader("content", bitmapShader)
            
            dispersionPaint.shader = shader
            dispersionPaint.blendMode = BlendMode.OVERLAY
            canvas.drawRect(0f, 0f, width, height, dispersionPaint)
        }
    }
    
    private fun drawBasicGlassEffect(canvas: Canvas, bgBitmap: Bitmap, width: Float, height: Float) {
        // Fallback implementation for older Android versions
        val bitmapShader = BitmapShader(bgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        
        // Apply basic blur effect if available
        val blur = blurEffect
        if (blur != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            paint.shader = bitmapShader
            paint.maskFilter = BlurMaskFilter(blur.radius, BlurMaskFilter.Blur.NORMAL)
        } else {
            paint.shader = bitmapShader
            paint.maskFilter = null
        }
        
        canvas.drawRect(0f, 0f, width, height, paint)
        
        // Add a subtle tint to simulate glass effect
        paint.shader = null
        paint.color = "#1AFFFFFF".toColorInt() // Subtle white tint
        canvas.drawRect(0f, 0f, width, height, paint)
    }
    
    private fun drawHighlightEffect(canvas: Canvas, width: Float, height: Float) {
        val highlight = highlightEffect ?: return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            drawAdvancedHighlight(canvas, width, height, highlight)
        } else {
            drawBasicHighlight(canvas, width, height, highlight)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun drawAdvancedHighlight(canvas: Canvas, width: Float, height: Float, highlight: HighlightEffect) {
        val shader = obtainRuntimeShader("Highlight", LiquidGlassShaders.DEFAULT_HIGHLIGHT_SHADER)
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("cornerRadii", getCornerRadiiArray())
        shader.setFloatUniform("angle", highlight.angle)
        shader.setFloatUniform("falloff", highlight.falloff)

        // Solid color input shader for highlights
        val solidShader = BitmapShader(
            createBitmap(1, 1).apply { setPixel(0,0,highlight.color) },
            Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
        )
        shader.setInputShader("content", solidShader)

        highlightPaint.shader = shader
        highlightPaint.alpha = (highlight.alpha * 255).toInt()

        val clipPath = createRoundedRectPath(width, height)
        canvas.withClip(clipPath) {
            drawRect(0f, 0f, width, height, highlightPaint)
        }
    }


    private fun drawBasicHighlight(canvas: Canvas, width: Float, height: Float, highlight: HighlightEffect) {
        // Fallback highlight implementation
        val lightDirection = PointF(cos(highlight.angle), sin(highlight.angle))
        
        // Create a simple gradient based on the light direction
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val gradientRadius = maxOf(width, height) * 0.7f
        
        val startX = centerX - lightDirection.x * gradientRadius * 0.3f
        val startY = centerY - lightDirection.y * gradientRadius * 0.3f
        val endX = centerX + lightDirection.x * gradientRadius * 0.7f
        val endY = centerY + lightDirection.y * gradientRadius * 0.7f
        
        val gradient = RadialGradient(
            startX, startY, gradientRadius,
            intArrayOf(
                Color.argb((highlight.alpha * 255 * 0.8f).toInt(), Color.red(highlight.color), Color.green(highlight.color), Color.blue(highlight.color)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        
        highlightPaint.shader = gradient
        
        val clipPath = createRoundedRectPath(width, height)
        canvas.withClip(clipPath) {
            drawRect(0f, 0f, width, height, highlightPaint)
        }
    }
    
    /**
     * Draws the surface tint layer over the backdrop glass effect.
     * Matches Compose's onDrawSurface parameter in drawBackdrop.
     * Default: Black 5% alpha (Color.Black.copy(0.05f))
     */
    private fun drawSurfaceLayer(canvas: Canvas, width: Float, height: Float) {
        surfacePaint.color = surfaceColor
        canvas.drawRect(0f, 0f, width, height, surfacePaint)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun drawInnerShadowEffect(canvas: Canvas, width: Float, height: Float) {
        val innerShadow = innerShadowEffect ?: return
        val shader = obtainRuntimeShader("InnerShadow", LiquidGlassShaders.INNER_SHADOW_SHADER)

        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("cornerRadii", getCornerRadiiArray())
        shader.setFloatUniform("radius", innerShadow.radius)
        shader.setFloatUniform("alpha", innerShadow.alpha)

        val baseShader = BitmapShader(
            createBitmap(1, 1).apply {
                eraseColor(Color.TRANSPARENT)
            },
            Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
        )
        shader.setInputShader("content", baseShader)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }

        val path = createRoundedRectPath(width, height)
        canvas.withClip(path) {
            drawRect(0f, 0f, width, height, paint)
        }
    }




    private fun createRoundedRectPath(width: Float, height: Float): Path {
        val path = Path()
        val radii = floatArrayOf(
            cornerRadiusTopLeft, cornerRadiusTopLeft,
            cornerRadiusTopRight, cornerRadiusTopRight,
            cornerRadiusBottomRight, cornerRadiusBottomRight,
            cornerRadiusBottomLeft, cornerRadiusBottomLeft
        )
        path.addRoundRect(RectF(0f, 0f, width, height), radii, Path.Direction.CW)
        return path
    }
    
    private fun getCornerRadiiArray(): FloatArray {
        return floatArrayOf(
            cornerRadiusTopLeft,
            cornerRadiusTopRight,
            cornerRadiusBottomRight,
            cornerRadiusBottomLeft
        )
    }
    
    private fun updateBlurEffect() {
        val blur = blurEffect
        // BlurMaskFilter requires radius > 0, otherwise it throws IllegalArgumentException
        if (blur != null && blur.radius > 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            paint.maskFilter = BlurMaskFilter(blur.radius, 
                when (blur.style) {
                    BlurEffect.BlurStyle.NORMAL -> BlurMaskFilter.Blur.NORMAL
                    BlurEffect.BlurStyle.SOLID -> BlurMaskFilter.Blur.SOLID
                    BlurEffect.BlurStyle.OUTER -> BlurMaskFilter.Blur.OUTER
                    BlurEffect.BlurStyle.INNER -> BlurMaskFilter.Blur.INNER
                }
            )
        } else {
            paint.maskFilter = null
        }
    }
    
    private fun updateHighlightEffect() {
        // Highlight effect is applied in onDraw
    }
    
    private fun updateShadowEffect() {
        // Shadow effect is applied in onDraw
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        
        // Unregister layout listener
        viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        
        // Clean up resources
        backgroundBitmap?.recycle()
        backgroundBitmap = null
        backgroundCanvas = null
        
        // Clear shader cache
        (this as RuntimeShaderCacheScope).let { scope ->
        }
    }
    
    private fun createColorMatrixColorFilter(colorFilter: ColorFilterEffect): ColorMatrixColorFilter {
        val brightness = colorFilter.brightness
        val contrast = colorFilter.contrast
        val saturation = colorFilter.saturation
        val alpha = colorFilter.alpha
        
        val invSat = 1f - saturation
        val r = 0.213f * invSat
        val g = 0.715f * invSat
        val b = 0.072f * invSat

        val c = contrast
        val t = (0.5f - c * 0.5f + brightness) * 255f
        val s = saturation

        val cr = c * r
        val cg = c * g
        val cb = c * b
        val cs = c * s

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                cr + cs, cg, cb, 0f, t,
                cr, cg + cs, cb, 0f, t,
                cr, cg, cb + cs, 0f, t,
                0f, 0f, 0f, alpha, 0f
            )
        )
        return ColorMatrixColorFilter(colorMatrix)
    }
    
    private fun createExposureColorFilter(exposure: ExposureAdjustmentEffect): ColorMatrixColorFilter {
        val scale = 2f.pow(exposure.ev / 2.2f)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, 0f,
                0f, scale, 0f, 0f, 0f,
                0f, 0f, scale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return ColorMatrixColorFilter(colorMatrix)
    }
    fun setGlassPreset(preset: Int) {
        // Apply preset programmatically
        applyPreset(preset)
        invalidate() // Force redraw
    }
}