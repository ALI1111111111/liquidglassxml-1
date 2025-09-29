package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import com.ali.funsol.glass.liquid.tech.liquidglass.ScriptC_refraction

/**
 * An effect that applies a lens-like refraction distortion to a [Bitmap].
 * This implementation uses a [RenderScript] displacement map.
 *
 * @param context The context used to create the RenderScript instance.
 */
class RefractionEffect(private val context: Context) : Effect {

    /** The intensity of the refraction effect. Recommended values are between 0.0 and 0.1. */
    var intensity: Float = 0f
    /** If true, a subtle parallax effect is added to simulate depth. */
    var hasDepthEffect: Boolean = false

    private var rs: RenderScript? = null

    override fun apply(bitmap: Bitmap): Bitmap {
        if (intensity <= 0) return bitmap

        if (rs == null) {
            rs = RenderScript.create(context)
        }
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        val script = ScriptC_refraction(rs)
        script.set_in_allocation(inputAllocation)
        script.set_intensity(intensity.coerceIn(0f, 0.1f)) // Keep intensity in a reasonable range
        script.set_width(bitmap.width)
        script.set_height(bitmap.height)
        script.set_has_depth_effect(if (hasDepthEffect) 1 else 0)

        script.forEach_root(outputAllocation)

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
