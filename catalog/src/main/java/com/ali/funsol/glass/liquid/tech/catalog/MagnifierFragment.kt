package com.ali.funsol.glass.liquid.tech.catalog

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.ali.funsol.glass.liquid.tech.catalog.databinding.FragmentMagnifierBinding

class MagnifierFragment : BaseDemoFragment<FragmentMagnifierBinding>(FragmentMagnifierBinding::inflate) {

    private var dX = 0f
    private var dY = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.magnifierView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            true
        }
    }
}
