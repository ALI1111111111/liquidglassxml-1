package com.kyant.backdrop.xml

import android.graphics.Canvas
import android.graphics.Path

internal class ShadowController {
    private var shadow: Shadow = Shadow.None

    fun setShadow(shadow: Shadow) {
        this.shadow = shadow
    }

    fun draw(canvas: Canvas, path: Path) {
        shadow.draw(canvas, path)
    }
}
