package com.ali.funsol.glass.liquid.tech.catalog

import android.os.Bundle
import android.view.View
import com.ali.funsol.glass.liquid.tech.catalog.databinding.FragmentLockScreenBinding
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.BlurEffect
import com.ali.funsol.glass.liquid.tech.liquidglass.effects.RefractionEffect

class LockScreenFragment : BaseDemoFragment<FragmentLockScreenBinding>(FragmentLockScreenBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationView.addContentEffect(BlurEffect(requireContext()).apply {
            radius = 5f
        })
        binding.notificationView.addContentEffect(RefractionEffect(requireContext()).apply {
            intensity = 0.02f
        })
    }
}
