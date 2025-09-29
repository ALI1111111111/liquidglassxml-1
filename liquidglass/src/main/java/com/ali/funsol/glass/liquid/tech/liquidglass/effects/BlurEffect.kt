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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For now, we'll stick with RenderScript for consistency.
            // A full RenderEffect implementation would require applying it to the View itself.
            rsBlur(bitmap)
        } else {
            rsBlur(bitmap)
        }
    }

    private fun rsBlur(src: Bitmap): Bitmap {
        if (rs == null) {
            rs = RenderScript.create(context)
        }
        val output = Bitmap.createBitmap(src.width, src.height, src.config)
        val inputAllocation = Allocation.createFromBitmap(rs, src)
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
