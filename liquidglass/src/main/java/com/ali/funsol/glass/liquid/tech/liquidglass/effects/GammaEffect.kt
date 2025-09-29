package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import com.ali.funsol.glass.liquid.tech.liquidglass.ScriptC_gamma

class GammaEffect(context: Context) : Effect {

    private val rs = RenderScript.create(context)
    private val script = ScriptC_gamma(rs)
    var power: Float = 1f

    override fun apply(bitmap: Bitmap): Bitmap {
        if (power == 1f) {
            return bitmap
        }

        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, output)

        script.set_gammaValue(power)
        script.forEach_applyGamma(inputAllocation, outputAllocation)

        outputAllocation.copyTo(output)
        inputAllocation.destroy()
        outputAllocation.destroy()

        return output
    }

    fun destroy() {
        rs.destroy()
    }
}
