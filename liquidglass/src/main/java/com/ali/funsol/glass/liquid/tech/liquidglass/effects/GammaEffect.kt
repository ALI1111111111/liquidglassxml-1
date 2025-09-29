package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import com.ali.funsol.glass.liquid.tech.liquidglass.ScriptC_gamma

class GammaEffect(private val context: Context) : Effect {

    var power: Float = 1f
    private var rs: RenderScript? = null

    override fun apply(bitmap: Bitmap): Bitmap {
        if (power <= 0) return bitmap

        if (rs == null) {
            rs = RenderScript.create(context)
        }
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        val script = ScriptC_gamma(rs)
        script.set_power(power)

        script.forEach_root(inputAllocation, outputAllocation)

        outputAllocation.copyTo(output)
        return output
    }

    fun destroy() {
        rs?.destroy()
        rs = null
    }
}
