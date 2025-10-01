package com.kyant.backdrop.catalog.xml.destinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.kyant.backdrop.catalog.xml.databinding.FragmentGlassPlaygroundBinding
import com.kyant.backdrop.xml.BackdropEffect

class GlassPlaygroundFragment : Fragment() {

    private var _binding: FragmentGlassPlaygroundBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlassPlaygroundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Add fallback surface for API <31
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            binding.glassView.onDrawSurface = { canvas ->
                val fallbackPaint = android.graphics.Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    alpha = 102 // 40% opacity
                }
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), fallbackPaint)
            }
        }
        
        setupControls()
    }

    private fun setupControls() {
        binding.cornerRadiusSlider.addOnChangeListener { _, value, _ ->
            binding.glassView.setCornerRadius(value * 128f * resources.displayMetrics.density)
        }

        binding.blurRadiusSlider.addOnChangeListener { _, value, _ ->
            updateEffects()
        }

        binding.refractionHeightSlider.addOnChangeListener { _, value, _ ->
            updateRefraction()
        }

        binding.refractionAmountSlider.addOnChangeListener { _, value, _ ->
            updateRefraction()
        }

        binding.dispersionIntensitySlider.addOnChangeListener { _, value, _ ->
            updateDispersion()
        }

        // Set initial values
        updateEffects()
        updateRefraction()
        updateDispersion()
    }

    private fun updateEffects() {
        val blurRadius = binding.blurRadiusSlider.value * resources.displayMetrics.density
        binding.glassView.setBackdropEffects(
            listOf(
                BackdropEffect.Blur(blurRadius, blurRadius)
            )
        )
    }

    private fun updateRefraction() {
        val height = binding.refractionHeightSlider.value * binding.glassView.height * 0.5f
        val amount = binding.refractionAmountSlider.value * binding.glassView.height
        binding.glassView.setRefraction(height, amount, 0.2f)
    }

    private fun updateDispersion() {
        val intensity = binding.dispersionIntensitySlider.value
        // This is a simplified approximation. A true dispersion effect would require a custom shader.
        val dispersionAmount = intensity * 20f
        binding.glassView.setDispersion(binding.glassView.height * 0.1f, dispersionAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
