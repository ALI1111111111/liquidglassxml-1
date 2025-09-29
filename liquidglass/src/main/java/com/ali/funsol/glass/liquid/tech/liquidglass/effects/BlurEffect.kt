package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.RequiresApi

/**
 * An effect that applies a Gaussian blur to a [Bitmap].
 * This implementation uses [RenderScript] for backward compatibility.
 *
 * @param context The context used to create the RenderScript instance.
 */
class BlurEffect(private val context: Context) : Effect {

    /** The radius of the blur. Must be between 0.1 and 25.0. */
    var radius: Float = 25f

    private var rs: RenderScript? = null

    override fun apply(bitmap: Bitmap): Bitmap {
        if (radius <= 0) return bitmap

        if (rs == null) {
            rs = RenderScript.create(context)
        }
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blurScript.setRadius(radius.coerceIn(0.1f, 25f))
        blurScript.setInput(inputAllocation)
        blurScript.forEach(outputAllocation)

        outputAllocation.copyTo(output)
        return output
    }

    /**
     * Destroys the RenderScript instance to free up native resources.
     * Should be called when the effect is no longer needed.
     */
    fun destroy() {
        rs?.destroy()
        rs = null
    }
}
