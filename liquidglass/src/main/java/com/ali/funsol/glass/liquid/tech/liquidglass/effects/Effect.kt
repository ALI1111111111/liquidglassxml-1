package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.Bitmap

interface Effect {
    fun apply(bitmap: Bitmap): Bitmap
}
