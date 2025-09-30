package com.kyant.backdrop.xml

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
internal class InverseDrawingController {
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * Applies all inverse drawing effects to the canvas.
     * @param canvas The canvas to draw on.
     * @param effects The list of all effects to filter from.
     */
    fun draw(canvas: Canvas, effects: List<BackdropEffect>) {
        effects.forEach { effect ->
            if (effect is BackdropEffect.Inverse) {
                canvas.drawPath(effect.path, clearPaint)
            }
        }
    }

    /**
     * Filters out the inverse effects from a list, as they are handled separately.
     * @param effects The list of effects to filter.
     * @return A new list containing only the non-inverse effects.
     */
    fun filterInverseEffects(effects: List<BackdropEffect>): List<BackdropEffect> {
        return effects.filterNot { it is BackdropEffect.Inverse }
    }
}
