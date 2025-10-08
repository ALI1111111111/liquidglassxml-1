package com.kyant.backdrop.xml.backdrop

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Core interface for backdrop sources in the XML version.
 * This is the equivalent of the Compose Backdrop interface.
 */
interface XmlBackdrop {
    
    /**
     * Whether this backdrop requires coordinate information for rendering
     */
    val isCoordinatesDependent: Boolean get() = false
    
    /**
     * Draws the backdrop content to the provided canvas
     */
    fun drawBackdrop(canvas: Canvas, width: Float, height: Float)
}

/**
 * Empty backdrop that draws nothing - equivalent to EmptyBackdrop in Compose
 */
object EmptyXmlBackdrop : XmlBackdrop {
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        // Draw nothing
    }
}

/**
 * Canvas-based backdrop that uses a custom drawing function
 * Equivalent to CanvasBackdrop in Compose
 */
class CanvasXmlBackdrop(
    private val onDraw: (Canvas, Float, Float) -> Unit
) : XmlBackdrop {
    
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        onDraw(canvas, width, height)
    }
}

/**
 * Drawable-based backdrop that renders a Drawable as the background
 */
class DrawableXmlBackdrop(
    private val drawable: Drawable
) : XmlBackdrop {
    
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        drawable.setBounds(0, 0, width.toInt(), height.toInt())
        drawable.draw(canvas)
    }
}

/**
 * View-based backdrop that captures content from another view
 */
class ViewXmlBackdrop(
    private val sourceView: View
) : XmlBackdrop {
    
    override val isCoordinatesDependent: Boolean = true
    
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        sourceView.draw(canvas)
    }
}

/**
 * Combined backdrop that layers multiple backdrops
 * Equivalent to CombinedBackdrop in Compose
 */
class CombinedXmlBackdrop(
    private val backdrops: List<XmlBackdrop>
) : XmlBackdrop {
    
    override val isCoordinatesDependent: Boolean 
        get() = backdrops.any { it.isCoordinatesDependent }
    
    override fun drawBackdrop(canvas: Canvas, width: Float, height: Float) {
        backdrops.forEach { backdrop ->
            backdrop.drawBackdrop(canvas, width, height)
        }
    }
    
    companion object {
        /**
         * Creates a combined backdrop from multiple backdrop sources
         */
        fun of(vararg backdrops: XmlBackdrop): CombinedXmlBackdrop {
            return CombinedXmlBackdrop(backdrops.toList())
        }
    }
}