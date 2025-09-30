package com.kyant.backdrop.xml

import android.graphics.Canvas
import android.graphics.Path

internal class InnerShadowController {
    private var innerShadow: InnerShadow = InnerShadow.None

    fun setInnerShadow(innerShadow: InnerShadow) {
        this.innerShadow = innerShadow
    }

    fun draw(canvas: Canvas, path: Path, width: Float, height: Float) {
        innerShadow.draw(canvas, path, width, height)
    }
}
