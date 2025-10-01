package com.kyant.backdrop.xml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * A custom view that creates liquid glass effects with backward compatibility.
 * 
 * Supported API levels:
 * - API 21-30: Basic glass effect using Canvas blur and color filters
 * - API 31+: Full RenderEffect support
 * - API 33+: RuntimeShader support for advanced effects (refraction, dispersion)
 */
open class LiquidGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // region Public Drawing Hooks
    var onDrawBehind: ((Canvas) -> Unit)? = null
    var onDrawSurface: ((Canvas) -> Unit)? = null
    /**
     * A simple callback that is invoked after the default backdrop has been drawn.
     * For more control, use the wrapping [onDrawBackdrop] hook.
     */
    var onDrawOnBackdrop: ((Canvas) -> Unit)? = null
    /**
     * A powerful wrapping hook that gives you full control over the backdrop rendering pipeline.
     * The lambda receives the canvas and a `drawDefault` function. You are responsible for
     * calling `drawDefault(canvas)` if you want the standard glass effect to be rendered.
     * This allows you to draw under and over the default rendering.
     */
    var onDrawBackdrop: ((canvas: Canvas, drawDefault: (Canvas) -> Unit) -> Unit)? = null
    var onDrawOnContent: ((Canvas) -> Unit)?
        get() = contentHost.onDrawOnContent
        set(value) {
            contentHost.onDrawOnContent = value
        }
    var onDrawFront: ((Canvas) -> Unit)? = null
    // endregion

    var animationDuration: Long
        get() = animationController.duration
        set(value) {
            animationController.duration = value
        }

    private val outlineController = OutlineController()
    private val shaderController = ShaderController()
    private val shadowController = ShadowController()
    private val innerShadowController = InnerShadowController()

    private var layerId: String? = null
    private var backdropSource: BackdropSource = BackdropSource.Default
    private var layerBackdrop: LayerBackdrop? = null
    private var viewBitmap: Bitmap? = null
    private val animationController = AnimationController {
        // When an animation updates, invalidate the backdrop view
        backdropView.invalidate()
    }
    private val backdropView: BackdropView
    private val contentHost: ContentHostView
    private val locationOnScreen = IntArray(2)

    private val layoutChangeListener =
        OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateGlobalCoordinates()
        }

    /**
     * A custom View that renders the entire backdrop.
     */
    private inner class BackdropView(context: Context) : View(context) {
        init {
            // Important for custom drawing to work
            setWillNotDraw(false)
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            val hook = onDrawBackdrop
            if (hook != null) {
                hook(canvas, this::drawDefault)
            } else {
                drawDefault(canvas)
            }
        }

        private fun drawDefault(canvas: Canvas) {
            onDrawBehind?.invoke(canvas)

            // Prepare the bitmap we might need for rendering sources or exporting our output
            if (needsBitmap() && (viewBitmap == null || viewBitmap!!.width != width || viewBitmap!!.height != height)) {
                viewBitmap?.recycle()
                viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            // Render the source to a bitmap if necessary
            val sourceBitmap = renderSourceToBitmap(backdropSource)
            if (sourceBitmap != null) {
                shaderController.setInputShader(BitmapShader(sourceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
                updateEffects(invalidateBackdrop = false) // Effects are already being updated
            }

            val targetCanvas = if (layerBackdrop != null) {
                Canvas(viewBitmap!!)
            } else {
                canvas
            }

            // Draw the outer shadow behind everything else
            shadowController.draw(targetCanvas, outlineController.getPath(width.toFloat(), height.toFloat()))

            // Draw the main view content (which triggers the RenderEffect chain)
            super.draw(targetCanvas)

            // Draw the inner shadow on top
            innerShadowController.draw(
                targetCanvas,
                outlineController.getPath(width.toFloat(), height.toFloat()),
                width.toFloat(),
                height.toFloat()
            )

            if (layerBackdrop != null) {
                layerBackdrop!!.update(viewBitmap!!)
                canvas.drawBitmap(viewBitmap!!, 0f, 0f, null)
            }

            onDrawOnBackdrop?.invoke(canvas)
            shaderController.drawInverseBackdropEffects(canvas)
        }
    }

    /**
     * A custom FrameLayout that hosts the content and provides a drawing hook.
     */
    private class ContentHostView(context: Context) : FrameLayout(context) {
        var onDrawOnContent: ((Canvas) -> Unit)? = null
        var drawInverseContentEffects: ((Canvas) -> Unit)? = null
        var shouldDrawContent: Boolean = true

        init {
            // Important for custom drawing to work
            setWillNotDraw(false)
        }

        override fun draw(canvas: Canvas) {
            if (shouldDrawContent) {
                super.draw(canvas)
            }
        }

        override fun dispatchDraw(canvas: Canvas) {
            super.dispatchDraw(canvas)
            onDrawOnContent?.invoke(canvas)
            drawInverseContentEffects?.invoke(canvas)
        }
    }

    init {
        backdropView = BackdropView(context)
        super.addView(backdropView, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        contentHost = ContentHostView(context)
        super.addView(contentHost, 1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        contentHost.drawInverseContentEffects = { canvas ->
            shaderController.drawInverseContentEffects(canvas)
        }

        // This is important for custom drawing to work
        setWillNotDraw(false)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LiquidGlassView,
            0, 0
        ).apply {
            try {
                val cornerRadius = getDimension(R.styleable.LiquidGlassView_cornerRadius, 0f)
                if (cornerRadius > 0) {
                    setCornerRadius(cornerRadius)
                } else {
                    setCornerRadii(
                        getDimension(R.styleable.LiquidGlassView_topLeftCornerRadius, 0f),
                        getDimension(R.styleable.LiquidGlassView_topRightCornerRadius, 0f),
                        getDimension(R.styleable.LiquidGlassView_bottomRightCornerRadius, 0f),
                        getDimension(R.styleable.LiquidGlassView_bottomLeftCornerRadius, 0f)
                    )
                }
                shaderController.setRefraction(
                    getDimension(R.styleable.LiquidGlassView_refractionHeight, 0f),
                    getDimension(R.styleable.LiquidGlassView_refractionAmount, 0f),
                    getFloat(R.styleable.LiquidGlassView_depthEffect, 0f)
                )
                shaderController.setDispersion(
                    getDimension(R.styleable.LiquidGlassView_dispersionHeight, 0f),
                    getDimension(R.styleable.LiquidGlassView_dispersionAmount, 0f)
                )
                shaderController.setHighlight(
                    getFloat(R.styleable.LiquidGlassView_highlightAngle, 0f),
                    getFloat(R.styleable.LiquidGlassView_highlightFalloff, 1f),
                    HighlightType.values()[getInt(R.styleable.LiquidGlassView_highlightType, 0)]
                )
                shadowController.setShadow(
                    DefaultShadow(
                        elevation = getDimension(R.styleable.LiquidGlassView_shadowElevation, 0f),
                        color = getColor(R.styleable.LiquidGlassView_shadowColor, 0xFF000000.toInt()),
                        offsetX = getDimension(R.styleable.LiquidGlassView_shadowOffsetX, 0f),
                        offsetY = getDimension(R.styleable.LiquidGlassView_shadowOffsetY, 0f)
                    )
                )
                innerShadowController.setInnerShadow(
                    DefaultInnerShadow(
                        elevation = getDimension(R.styleable.LiquidGlassView_innerShadowElevation, 0f),
                        color = getColor(R.styleable.LiquidGlassView_innerShadowColor, 0xFF000000.toInt()),
                        offsetX = getDimension(R.styleable.LiquidGlassView_innerShadowOffsetX, 0f),
                        offsetY = getDimension(R.styleable.LiquidGlassView_innerShadowOffsetY, 0f)
                    )
                )
                layerId = getString(R.styleable.LiquidGlassView_layerId)
                animationController.duration = getInt(R.styleable.LiquidGlassView_animationDuration, 0).toLong()
                setDrawContent(getBoolean(R.styleable.LiquidGlassView_drawContent, true))
            } finally {
                recycle()
            }
        }

        layerId?.let {
            layerBackdrop = LayerController.getLayer(it)
        }

        outlineProvider = outlineController.viewOutlineProvider
        clipToOutline = true

        updateEffects()
    }

    // region View Overrides for Content Host
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child == contentHost || child == backdropView) {
            super.addView(child, index, params)
            return
        }
        contentHost.addView(child, index, params)
    }

    override fun removeView(child: View?) {
        if (child == contentHost || child == backdropView) {
            super.removeView(child)
            return
        }
        contentHost.removeView(child)
    }

    override fun removeAllViews() {
        contentHost.removeAllViews()
    }
    // endregion

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateBackdropSource()
        updateCoordinateTracking()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateCoordinateTracking(forceRemove = true)
        layerId?.let {
            LayerController.releaseLayer(it)
        }
    }

    private fun needsBitmap(): Boolean {
        return layerBackdrop != null || backdropSource !is BackdropSource.Default
    }

    private fun renderSourceToBitmap(source: BackdropSource): Bitmap? {
        if (source is BackdropSource.Default) return null

        val bitmap = viewBitmap ?: return null
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR) // Clear the bitmap

        when (source) {
            is BackdropSource.DrawableSource -> {
                source.drawable.setBounds(0, 0, width, height)
                source.drawable.draw(canvas)
            }
            is BackdropSource.Layer -> {
                val layerBitmap = LayerController.getLayer(source.layerId).bitmap
                if (layerBitmap != null) {
                    canvas.drawBitmap(layerBitmap, 0f, 0f, null)
                }
            }
            is BackdropSource.CombinedSource -> {
                source.sources.forEach { subSource ->
                    // Recursively render each sub-source.
                    // Note: This implementation draws each source over the previous one.
                    // A more advanced version might use PorterDuff modes.
                    val subBitmap = renderSourceToBitmap(subSource)
                    if (subBitmap != null) {
                        canvas.drawBitmap(subBitmap, 0f, 0f, null)
                    }
                }
            }
            is BackdropSource.Default -> { /* Handled above */ }
        }
        return bitmap
    }

    private fun updateEffects(invalidateBackdrop: Boolean = true) {
        shaderController.setShape(
            outlineController.getPath(width.toFloat(), height.toFloat()),
            width,
            height,
            outlineController.isRoundRect()
        )
        shaderController.updateSize(width.toFloat(), height.toFloat())
        if (outlineController.isRoundRect()) {
            shaderController.updateCornerRadii(outlineController.getCornerRadii())
        }
        updateGlobalCoordinates()
        
        // Only apply RenderEffect on API 31+ (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            backdropView.setRenderEffect(shaderController.createBackdropEffect())
            contentHost.setRenderEffect(shaderController.createContentEffect())
        }
        
        if (invalidateBackdrop) {
            backdropView.invalidate()
        }
    }

    fun setCornerRadius(radius: Float) {
        setCornerRadii(radius, radius, radius, radius)
    }

    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        val from = outlineController.getCornerRadii()
        val to = floatArrayOf(topLeft, topRight, bottomRight, bottomLeft)
        animationController.animate(from, to) {
            outlineController.setCornerRadii(it[0], it[1], it[2], it[3])
            backdropView.invalidateOutline()
        }
    }

    fun setShapePath(path: Path) {
        // Note: Path morphing animations are complex and outside the current scope.
        // This will set the shape directly.
        outlineController.setShapePath(path)
        backdropView.invalidateOutline()
        updateEffects()
    }

    fun setRefraction(height: Float, amount: Float, depthEffect: Float) {
        val from = shaderController.getRefraction()
        val to = floatArrayOf(height, amount, depthEffect)
        animationController.animate(from, to) {
            shaderController.setRefraction(it[0], it[1], it[2])
        }
    }

    fun setDispersion(height: Float, amount: Float) {
        val from = shaderController.getDispersion()
        val to = floatArrayOf(height, amount)
        animationController.animate(from, to) {
            shaderController.setDispersion(it[0], it[1])
        }
    }

    fun setHighlight(angle: Float, falloff: Float, type: HighlightType) {
        val from = shaderController.getHighlight()
        val to = floatArrayOf(angle, falloff, type.ordinal.toFloat())
        animationController.animate(from, to) {
            shaderController.setHighlight(it[0], it[1], HighlightType.values()[it[2].toInt()])
        }
    }

    /**
     * Sets the [Shadow] to be drawn behind the backdrop.
     * @param shadow The [Shadow] implementation to use. Can be a [DefaultShadow] or a custom implementation.
     */
    fun setShadow(shadow: Shadow) {
        shadowController.setShadow(shadow)
        backdropView.invalidate()
    }

    /**
     * Sets the [InnerShadow] to be drawn inside the backdrop.
     * @param innerShadow The [InnerShadow] implementation to use. Can be a [DefaultInnerShadow] or a custom implementation.
     */
    fun setInnerShadow(innerShadow: InnerShadow) {
        innerShadowController.setInnerShadow(innerShadow)
        backdropView.invalidate()
    }

    fun setDrawContent(drawContent: Boolean) {
        contentHost.shouldDrawContent = drawContent
        contentHost.invalidate()
    }

    fun setBackdropSource(source: BackdropSource) {
        this.backdropSource = source
        if (isAttachedToWindow) {
            updateBackdropSource()
            updateCoordinateTracking()
        }
    }

    private fun updateBackdropSource() {
        when (val source = backdropSource) {
            is BackdropSource.Default -> {
                shaderController.setInputShader(null)
                updateEffects()
            }
            is BackdropSource.Layer -> {
                // The drawing logic will now handle this by rendering the layer to a bitmap
                backdropView.invalidate()
            }
            is BackdropSource.DrawableSource -> {
                // The drawing logic will handle this
                backdropView.invalidate()
            }
            is BackdropSource.CombinedSource -> {
                // The drawing logic will handle this
                backdropView.invalidate()
            }
        }
    }

    fun setBackdropEffects(effects: List<BackdropEffect>) {
        shaderController.setBackdropEffects(effects)
        updateEffects()
        if (isAttachedToWindow) updateCoordinateTracking()
    }

    fun setContentEffects(effects: List<BackdropEffect>) {
        shaderController.setContentEffects(effects)
        updateEffects()
        if (isAttachedToWindow) updateCoordinateTracking()
    }

    private fun needsCoordinateTracking(): Boolean {
        // In a more advanced implementation, BackdropSource itself could declare this.
        // For now, we assume only runtime effects need it.
        return shaderController.backdropEffects.any { it.needsGlobalCoordinates } ||
                shaderController.contentEffects.any { it.needsGlobalCoordinates }
    }

    private fun updateCoordinateTracking(forceRemove: Boolean = false) {
        val needsTracking = !forceRemove && needsCoordinateTracking()
        removeOnLayoutChangeListener(layoutChangeListener)
        if (needsTracking) {
            addOnLayoutChangeListener(layoutChangeListener)
            updateGlobalCoordinates()
        }
    }

    private fun updateGlobalCoordinates() {
        if (needsCoordinateTracking()) {
            getLocationOnScreen(locationOnScreen)
            shaderController.updateGlobalCoordinates(locationOnScreen[0].toFloat(), locationOnScreen[1].toFloat())
        } else {
            shaderController.updateGlobalCoordinates(0f, 0f)
        }
    }

    // region Convenience Effect Methods
    /**
     * Adds a blur effect to the backdrop.
     * @param radius Blur radius in pixels
     */
    fun addBlurEffect(radius: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val effects = shaderController.backdropEffects.toMutableList()
        effects.add(BackdropEffect.Blur(radius, radius))
        setBackdropEffects(effects)
    }

    /**
     * Adds vibrancy effect to increase color saturation.
     */
    fun addVibrancyEffect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val effects = shaderController.backdropEffects.toMutableList()
        effects.add(BackdropEffect.Vibrancy)
        setBackdropEffects(effects)
    }

    /**
     * Adds color controls effect.
     * @param brightness Brightness adjustment (-1.0 to 1.0)
     * @param contrast Contrast multiplier (0.0 to 2.0)
     * @param saturation Saturation multiplier (0.0 to 2.0)
     */
    fun addColorControlsEffect(brightness: Float = 0f, contrast: Float = 1f, saturation: Float = 1f) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val effects = shaderController.backdropEffects.toMutableList()
        effects.add(BackdropEffect.ColorControls(brightness, contrast, saturation))
        setBackdropEffects(effects)
    }

    /**
     * Adds gamma adjustment effect.
     * @param power Gamma power (0.5 = lighter, 2.0 = darker, 1.0 = no change)
     */
    fun addGammaEffect(power: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val effects = shaderController.backdropEffects.toMutableList()
        effects.add(BackdropEffect.GammaAdjustment(power))
        setBackdropEffects(effects)
    }

    /**
     * Adds opacity effect.
     * @param alpha Opacity level (0.0 to 1.0)
     */
    fun addOpacityEffect(alpha: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val effects = shaderController.backdropEffects.toMutableList()
        effects.add(BackdropEffect.Opacity(alpha))
        setBackdropEffects(effects)
    }

    /**
     * Clears all backdrop effects.
     */
    fun clearBackdropEffects() {
        setBackdropEffects(emptyList())
    }

    /**
     * Clears all content effects.
     */
    fun clearContentEffects() {
        setContentEffects(emptyList())
    }
    // endregion

    // region Backdrop Layer Transformations
    fun setBackdropAlpha(alpha: Float) { backdropView.alpha = alpha }
    fun getBackdropAlpha(): Float = backdropView.alpha

    fun setBackdropScaleX(scaleX: Float) { backdropView.scaleX = scaleX }
    fun getBackdropScaleX(): Float = backdropView.scaleX

    fun setBackdropScaleY(scaleY: Float) { backdropView.scaleY = scaleY }
    fun getBackdropScaleY(): Float = backdropView.scaleY

    fun setBackdropRotation(rotation: Float) { backdropView.rotation = rotation }
    fun getBackdropRotation(): Float = backdropView.rotation

    fun setBackdropRotationX(rotationX: Float) { backdropView.rotationX = rotationX }
    fun getBackdropRotationX(): Float = backdropView.rotationX

    fun setBackdropRotationY(rotationY: Float) { backdropView.rotationY = rotationY }
    fun getBackdropRotationY(): Float = backdropView.rotationY

    fun setBackdropTranslationX(translationX: Float) { backdropView.translationX = translationX }
    fun getBackdropTranslationX(): Float = backdropView.translationX

    fun setBackdropTranslationY(translationY: Float) { backdropView.translationY = translationY }
    fun getBackdropTranslationY(): Float = backdropView.translationY
    // endregion
}
