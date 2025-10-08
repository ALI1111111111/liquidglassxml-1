package com.kyant.backdrop.xml.extensions

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import com.kyant.backdrop.xml.BackdropXml
import com.kyant.backdrop.xml.backdrop.XmlBackdrop
import com.kyant.backdrop.xml.utils.LiquidGlassEffectBuilder
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import com.kyant.backdrop.xml.views.LiquidGlassView

/**
 * Extension functions for easier liquid glass effects configuration.
 * These extensions make the XML version as convenient as the Compose version.
 */

// ========== LiquidGlassView Extensions ==========

/**
 * Applies liquid glass effects using a builder pattern
 */
fun LiquidGlassView.liquidGlass(block: LiquidGlassEffectBuilder.() -> Unit) {
    BackdropXml.effectBuilder().apply(block).applyTo(this)
}

/**
 * Applies an iOS-style glass button effect
 */
fun LiquidGlassView.iosGlassButton(cornerRadius: Float = 12f) {
    BackdropXml.Presets.iosButton(cornerRadius).applyTo(this)
}

/**
 * Applies a Material Design glass card effect
 */
fun LiquidGlassView.materialGlassCard(cornerRadius: Float = 16f) {
    BackdropXml.Presets.materialCard(cornerRadius).applyTo(this)
}

/**
 * Applies a frosted glass overlay effect
 */
fun LiquidGlassView.frostedGlassOverlay(cornerRadius: Float = 8f) {
    BackdropXml.Presets.frostedOverlay(cornerRadius).applyTo(this)
}

/**
 * Applies a chromatic liquid glass effect with dispersion
 */
fun LiquidGlassView.chromaticLiquidGlass(cornerRadius: Float = 20f) {
    BackdropXml.Presets.chromaticGlass(cornerRadius).applyTo(this)
}

/**
 * Sets a drawable as the background source
 */
fun LiquidGlassView.backgroundDrawable(drawable: Drawable) {
    setBackgroundSource(drawable)
}

/**
 * Sets a view as the background source
 */
fun LiquidGlassView.backgroundView(view: View) {
    setBackgroundSource(view)
}

/**
 * Sets a custom backdrop as the background source
 */
fun LiquidGlassView.backdrop(backdrop: XmlBackdrop) {
    setBackgroundSource(backdrop)
}

// ========== LiquidGlassContainer Extensions ==========

/**
 * Applies liquid glass effects using a builder pattern
 */
fun LiquidGlassContainer.liquidGlass(block: LiquidGlassEffectBuilder.() -> Unit) {
    BackdropXml.effectBuilder().apply(block).applyTo(this)
}

/**
 * Applies an iOS-style glass button effect
 */
fun LiquidGlassContainer.iosGlassButton(cornerRadius: Float = 12f) {
    BackdropXml.Presets.iosButton(cornerRadius).applyTo(this)
}

/**
 * Applies a Material Design glass card effect
 */
fun LiquidGlassContainer.materialGlassCard(cornerRadius: Float = 16f) {
    BackdropXml.Presets.materialCard(cornerRadius).applyTo(this)
}

/**
 * Applies a frosted glass overlay effect
 */
fun LiquidGlassContainer.frostedGlassOverlay(cornerRadius: Float = 8f) {
    BackdropXml.Presets.frostedOverlay(cornerRadius).applyTo(this)
}

/**
 * Sets a view as the background source
 */
fun LiquidGlassContainer.backgroundView(view: View) {
    setBackgroundSource(view)
}

// ========== Drawable Extensions ==========

/**
 * Converts a Drawable to an XmlBackdrop
 */
fun Drawable.asBackdrop(): XmlBackdrop = BackdropXml.Backdrops.drawable(this)

// ========== View Extensions ==========

/**
 * Converts a View to an XmlBackdrop
 */
fun View.asBackdrop(): XmlBackdrop = BackdropXml.Backdrops.view(this)

/**
 * Applies liquid glass effects to any View by wrapping it in a LiquidGlassContainer
 */
fun View.withLiquidGlass(block: LiquidGlassEffectBuilder.() -> Unit): LiquidGlassContainer {
    val container = LiquidGlassContainer(context)
    container.addView(this)
    container.liquidGlass(block)
    return container
}

// ========== Canvas Extensions ==========

/**
 * Creates a canvas-based backdrop from a drawing function
 */
fun canvasBackdrop(onDraw: (Canvas, Float, Float) -> Unit): XmlBackdrop = 
    BackdropXml.Backdrops.canvas(onDraw)

// ========== Collection Extensions ==========

/**
 * Combines multiple XmlBackdrops into a single backdrop
 */
fun Collection<XmlBackdrop>.combined(): XmlBackdrop = 
    BackdropXml.Backdrops.combined(*this.toTypedArray())

/**
 * Combines multiple XmlBackdrops using the + operator
 */
operator fun XmlBackdrop.plus(other: XmlBackdrop): XmlBackdrop =
    BackdropXml.Backdrops.combined(this, other)