package com.kyant.backdrop.xml

import android.graphics.drawable.Drawable

/**
 * Defines the source of the content to which the liquid glass effect is applied.
 */
sealed class BackdropSource {
    /**
     * The default behavior. The view will apply its effect to the content rendered
     * behind it in the view hierarchy.
     */
    object Default : BackdropSource()

    /**
     * Uses the output of another [LiquidGlassView] as the source.
     * @property layerId The unique ID of the [LiquidGlassView] to use as the source,
     *                   set via the `app:layerId` attribute.
     */
    data class Layer(val layerId: String) : BackdropSource()

    /**
     * Uses a [Drawable] as the source. The drawable will be rendered to fill the bounds
     * of the view, and the effect will be applied to it.
     * @property drawable The drawable to use as the source.
     */
    data class DrawableSource(val drawable: Drawable) : BackdropSource()

    /**
     * Combines multiple sources into a single backdrop. The sources are rendered
     * sequentially, with each one drawing on top of the previous one.
     * @property sources The list of [BackdropSource]s to combine.
     */
    data class CombinedSource(val sources: List<BackdropSource>) : BackdropSource()
}
