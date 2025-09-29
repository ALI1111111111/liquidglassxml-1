package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import com.ali.funsol.glass.liquid.tech.liquidglass.ScriptC_dispersion

/**
 * An effect that applies a chromatic aberration or "dispersion" effect to a [Bitmap].
 * This splits the color channels slightly to create a prism-like visual.
 *
 * @param context The context used to create the RenderScript instance.
 */
class DispersionEffect(private val context: Context) : Effect {

    /** The intensity of the dispersion effect. Recommended values are between 0.0 and 0.1. */
    var intensity: Float = 0f

    private var rs: RenderScript? = null

    override fun apply(bitmap: Bitmap): Bitmap {
        if (intensity <= 0) return bitmap

        if (rs == null) {
            rs = RenderScript.create(context)
        }
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        val script = ScriptC_dispersion(rs)
        script._in_allocation = inputAllocation
        script._intensity = intensity.coerceIn(0f, 0.1f)
        script._width = bitmap.width
        script._height = bitmap.height

        script.forEach_disperse(inputAllocation, outputAllocation)

        outputAllocation.copyTo(output)
        return output
    }

    /**
     * Destroys the RenderScript instance to free up native resources.
     */
    fun destroy() {
        rs?.destroy()
        rs = null
    }
}
