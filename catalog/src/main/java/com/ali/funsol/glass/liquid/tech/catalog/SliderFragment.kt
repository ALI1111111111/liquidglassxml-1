package com.ali.funsol.glass.liquid.tech.catalog

import android.os.Bundle
import android.view.View
import com.ali.funsol.glass.liquid.tech.catalog.databinding.FragmentSliderBinding
import com.ali.funsol.glass.liquid.tech.liquidglass.LiquidSlider

class SliderFragment : BaseDemoFragment<FragmentSliderBinding>(FragmentSliderBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.slider.onValueChanged = { value ->
            binding.glassView.blurRadius = value
            binding.blurRadiusLabel.text = "Blur Radius: ${value.toInt()}"
        }
    }
}
