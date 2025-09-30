package com.kyant.backdrop.xml

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Paint
import android.view.View
import android.view.ViewOutlineProvider

internal class OutlineController {
    private var shapePath: Path? = null
    private var isRoundRect: Boolean = true

    val viewOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val path = getPath(view.width.toFloat(), view.height.toFloat())
            if (path.isConvex) {
                outline.setPath(path)
            }
        }
    }

    fun setShapePath(path: Path) {
        this.shapePath = path
        this.isRoundRect = false
    }

    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        this.shapePath = null // Clear custom path
        this.isRoundRect = true
        // Store radii for getCornerRadii() to work
        _currentRadii[0] = topLeft
        _currentRadii[1] = topRight
        _currentRadii[2] = bottomRight
        _currentRadii[3] = bottomLeft
    }

    fun getPath(width: Float, height: Float): Path {
        if (shapePath != null) return shapePath!!
        return Path().apply {
            addRoundRect(
                0f,
                0f,
                width,
                height,
                floatArrayOf(
                    _currentRadii[0], _currentRadii[0],
                    _currentRadii[1], _currentRadii[1],
                    _currentRadii[2], _currentRadii[2],
                    _currentRadii[3], _currentRadii[3]
                ),
                Path.Direction.CW
            )
        }
    }

    fun isRoundRect(): Boolean = isRoundRect

    private val _currentRadii = FloatArray(4) { 0f }
    fun getCornerRadii(): FloatArray {
        return _currentRadii.copyOf()
    }
}
