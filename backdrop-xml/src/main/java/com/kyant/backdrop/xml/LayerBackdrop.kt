package com.kyant.backdrop.xml

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader

internal class LayerBackdrop {
    var bitmap: Bitmap? = null
    var shader: BitmapShader? = null
        private set

    fun update(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    fun release() {
        bitmap?.recycle()
        bitmap = null
        shader = null
    }
}
