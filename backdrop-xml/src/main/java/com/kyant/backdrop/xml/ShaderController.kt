package com.kyant.backdrop.xml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.BitmapShader
import android.os.Build

/**
 * Controls shader-based effects with backward compatibility support.
 * For API < 31, uses legacy Canvas-based rendering.
 */
internal class ShaderController {
    // Original, optimized shaders for rounded rectangles (API 33+)
    private val rrRefractionShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(RoundedRectRefractionString)
    } else null
    
    private val rrDispersionShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(RoundedRectDispersionString)
    } else null
    
    private val rrSpecularHighlightShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(DefaultHighlightShaderString)
    } else null
    
    private val rrAmbientHighlightShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(AmbientHighlightShaderString)
    } else null

    // New, flexible shaders for generic shapes (API 33+)
    private val genericRefractionShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(GenericRefractionShaderString)
    } else null
    
    private val genericHighlightShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderCache.get(GenericHighlightShaderString)
    } else null

    private var isRoundRect: Boolean = true
    private var shapeMaskBitmap: Bitmap? = null
    private val shapeMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private var refractionHeight: Float = 0f
    private var refractionAmount: Float = 0f
    private var depthEffect: Float = 0f
    private var dispersionHeight: Float = 0f
    private var dispersionAmount: Float = 0f
    private var highlightAngle: Float = 0f
    private var highlightFalloff: Float = 1f
    private var highlightType: HighlightType = HighlightType.SPECULAR
    internal var backdropEffects: List<BackdropEffect> = emptyList()
    internal var contentEffects: List<BackdropEffect> = emptyList()
    private val inverseDrawingController = InverseDrawingController()

    fun setShape(path: Path, width: Int, height: Int, isRoundRect: Boolean) {
        this.isRoundRect = isRoundRect
        if (!isRoundRect) {
            // For generic shapes, we need to render the path to a bitmap mask
            if (width == 0 || height == 0) {
                // Don't try to create a bitmap if the view has no size yet.
                // The effects will be properly updated on the next layout pass.
                return
            }
            if (shapeMaskBitmap == null || shapeMaskBitmap!!.width != width || shapeMaskBitmap!!.height != height) {
                shapeMaskBitmap?.recycle()
                shapeMaskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
            }
            val canvas = Canvas(shapeMaskBitmap!!)
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            canvas.drawPath(path, shapeMaskPaint)

            val maskShader = BitmapShader(shapeMaskBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            genericRefractionShader?.setInputShader("shapeMask", maskShader)
            genericHighlightShader?.setInputShader("shapeMask", maskShader)
        }
    }

    fun setInputShader(shader: Shader?) {
        shader?.let {
            rrRefractionShader?.setInputShader("content", it)
            rrDispersionShader?.setInputShader("content", it)
            rrSpecularHighlightShader?.setInputShader("content", it)
            rrAmbientHighlightShader?.setInputShader("content", it)
            genericRefractionShader?.setInputShader("content", it)
            genericHighlightShader?.setInputShader("content", it)
        }
    }

    fun updateGlobalCoordinates(x: Float, y: Float) {
        // This is the root cause of the crash. The generic shaders do not have a "globalCoord" uniform.
        // We must only set this uniform on the shaders that actually declare it.
        if (isRoundRect) {
            rrRefractionShader?.setFloatUniform("globalCoord", x, y)
            rrDispersionShader?.setFloatUniform("globalCoord", x, y)
            rrSpecularHighlightShader?.setFloatUniform("globalCoord", x, y)
            rrAmbientHighlightShader?.setFloatUniform("globalCoord", x, y)
        }
        // No action is needed for the generic shaders as they don't use this uniform.
    }

    fun setBackdropEffects(effects: List<BackdropEffect>) {
        this.backdropEffects = effects
    }

    fun setContentEffects(effects: List<BackdropEffect>) {
        this.contentEffects = effects
    }

    fun setRefraction(height: Float, amount: Float, depthEffect: Float) {
        this.refractionHeight = height
        this.refractionAmount = amount
        this.depthEffect = depthEffect
        rrRefractionShader?.setFloatUniform("refractionHeight", height)
        rrRefractionShader?.setFloatUniform("refractionAmount", amount)
        rrRefractionShader?.setFloatUniform("depthEffect", depthEffect)
        genericRefractionShader?.setFloatUniform("refractionAmount", amount)
    }

    fun setDispersion(height: Float, amount: Float) {
        this.dispersionHeight = height
        this.dispersionAmount = amount
        rrDispersionShader?.setFloatUniform("dispersionHeight", height)
        rrDispersionShader?.setFloatUniform("dispersionAmount", amount)
    }

    fun setHighlight(angle: Float, falloff: Float, type: HighlightType) {
        this.highlightAngle = angle
        this.highlightFalloff = falloff
        this.highlightType = type
        val rrShader = if (type == HighlightType.SPECULAR) rrSpecularHighlightShader else rrAmbientHighlightShader
        rrShader?.setFloatUniform("angle", angle)
        rrShader?.setFloatUniform("falloff", falloff)
        genericHighlightShader?.setFloatUniform("angle", angle)
        genericHighlightShader?.setFloatUniform("falloff", falloff)
    }

    fun getRefraction(): FloatArray = floatArrayOf(refractionHeight, refractionAmount, depthEffect)
    fun getDispersion(): FloatArray = floatArrayOf(dispersionHeight, dispersionAmount)
    fun getHighlight(): FloatArray = floatArrayOf(highlightAngle, highlightFalloff, highlightType.ordinal.toFloat())

    fun updateSize(width: Float, height: Float) {
        rrRefractionShader?.setFloatUniform("size", width, height)
        rrDispersionShader?.setFloatUniform("size", width, height)
        rrSpecularHighlightShader?.setFloatUniform("size", width, height)
        rrAmbientHighlightShader?.setFloatUniform("size", width, height)
        genericRefractionShader?.setFloatUniform("size", width, height)
        genericHighlightShader?.setFloatUniform("size", width, height)
    }

    fun updateCornerRadii(radii: FloatArray) {
        rrRefractionShader?.setFloatUniform("cornerRadii", radii)
        rrDispersionShader?.setFloatUniform("cornerRadii", radii)
        rrSpecularHighlightShader?.setFloatUniform("cornerRadii", radii)
        rrAmbientHighlightShader?.setFloatUniform("cornerRadii", radii)
    }

    fun createBackdropEffect(): RenderEffect? {
        if (!isRoundRect) {
            // Use the generic shader pipeline for non-round-rect shapes
            // Note: Generic pipeline currently doesn't support dispersion or ambient highlight
            var effect: RenderEffect? = genericRefractionShader?.let { RenderEffect.createShaderEffect(it) }
            if (highlightFalloff > 0 && highlightType == HighlightType.SPECULAR) {
                genericHighlightShader?.let {
                    effect = RenderEffect.createChainEffect(
                        RenderEffect.createShaderEffect(it),
                        effect ?: return null
                    )
                }
            }
            // Apply runtime effects on top
            for (backdropEffect in inverseDrawingController.filterInverseEffects(backdropEffects)) {
                effect = RenderEffect.createChainEffect(
                    backdropEffect.createRenderEffect(),
                    effect ?: return null
                )
            }
            return effect
        }

        // Use the original, highly optimized pipeline for rounded rectangles
        var effect: RenderEffect? = rrRefractionShader?.let { RenderEffect.createShaderEffect(it) }
        if (dispersionAmount > 0) {
            rrDispersionShader?.let {
                effect = RenderEffect.createChainEffect(
                    RenderEffect.createShaderEffect(it),
                    effect ?: return null
                )
            }
        }
        if (highlightFalloff > 0) {
            val shader = if (highlightType == HighlightType.SPECULAR) rrSpecularHighlightShader else rrAmbientHighlightShader
            shader?.let {
                effect = RenderEffect.createChainEffect(
                    RenderEffect.createShaderEffect(it),
                    effect ?: return null
                )
            }
        }
        inverseDrawingController.filterInverseEffects(backdropEffects).forEach { backdropEffect ->
            effect = RenderEffect.createChainEffect(
                backdropEffect.createRenderEffect(),
                effect ?: return null
            )
        }
        return effect
    }

    fun createContentEffect(): RenderEffect? {
        val filteredEffects = inverseDrawingController.filterInverseEffects(contentEffects)
        if (filteredEffects.isEmpty()) return null
        var effect: RenderEffect? = null
        for (backdropEffect in filteredEffects) {
            effect = effect?.let {
                RenderEffect.createChainEffect(backdropEffect.createRenderEffect(), it)
            } ?: backdropEffect.createRenderEffect()
        }
        return effect
    }

    fun drawInverseBackdropEffects(canvas: Canvas) {
        inverseDrawingController.draw(canvas, backdropEffects)
    }

    fun drawInverseContentEffects(canvas: Canvas) {
        inverseDrawingController.draw(canvas, contentEffects)
    }
}
