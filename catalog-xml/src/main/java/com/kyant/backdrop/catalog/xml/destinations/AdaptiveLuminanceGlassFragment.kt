package com.kyant.backdrop.catalog.xml.destinations

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kyant.backdrop.catalog.xml.databinding.FragmentAdaptiveLuminanceGlassBinding
import com.kyant.backdrop.xml.BackdropEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.IntBuffer
import kotlin.math.pow

class AdaptiveLuminanceGlassFragment : Fragment() {

    private var _binding: FragmentAdaptiveLuminanceGlassBinding? = null
    private val binding get() = _binding!!

    private var currentLuminance = 0.5f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdaptiveLuminanceGlassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startLuminanceCalculation()
    }

    private fun startLuminanceCalculation() {
        lifecycleScope.launch {
            while (isActive) {
                delay(200) // Capture every 200ms
                val root = view?.rootView ?: continue
                val bitmap = withContext(Dispatchers.IO) {
                    val location = IntArray(2)
                    binding.glassView.getLocationOnScreen(location)
                    val sourceBitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
                    root.draw(android.graphics.Canvas(sourceBitmap))
                    val croppedBitmap = Bitmap.createBitmap(
                        sourceBitmap,
                        location[0],
                        location[1],
                        binding.glassView.width,
                        binding.glassView.height
                    )
                    croppedBitmap.scale(5, 5, false)
                }
                val newLuminance = calculateAverageLuminance(bitmap)
                animateLuminance(newLuminance)
            }
        }
    }

    private fun calculateAverageLuminance(bitmap: Bitmap): Float {
        val buffer = IntBuffer.allocate(bitmap.width * bitmap.height)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.rewind()
        var totalLuminance = 0.0
        while (buffer.hasRemaining()) {
            totalLuminance += ColorUtils.calculateLuminance(buffer.get())
        }
        return (totalLuminance / (bitmap.width * bitmap.height)).toFloat()
    }

    private fun animateLuminance(newLuminance: Float) {
        ValueAnimator.ofFloat(currentLuminance, newLuminance).apply {
            duration = 1000
            addUpdateListener {
                val animatedLuminance = it.animatedValue as Float
                updateEffects(animatedLuminance)
                binding.luminanceText.text = "luminance:\n${"%.2f".format(animatedLuminance)}"
            }
            start()
        }
        currentLuminance = newLuminance
    }

    private fun updateEffects(luminance: Float) {
        val l = (luminance * 2f - 1f).let { it.pow(2) * if (it > 0) 1 else -1 }
        val brightness = if (l > 0f) lerp(0.1f, 0.5f, l) else lerp(0.1f, -0.2f, -l)
        val contrast = if (l > 0f) lerp(1f, 0f, l) else 1f
        val blurRadius = if (l > 0f) lerp(8f, 16f, l) else lerp(8f, 2f, -l)

        binding.glassView.setBackdropEffects(
            listOf(
                BackdropEffect.Vibrancy,
                BackdropEffect.Blur(blurRadius, blurRadius)
                // Note: Brightness and contrast are not directly available as BackdropEffects.
                // This would require a custom shader or a more complex ColorFilter.
                // Vibrancy is used to approximate the effect.
            )
        )

        val targetTextColor = if (luminance > 0.5f) Color.BLACK else Color.WHITE
        ValueAnimator.ofObject(ArgbEvaluator(), binding.luminanceText.currentTextColor, targetTextColor).apply {
            duration = 1000
            addUpdateListener {
                binding.luminanceText.setTextColor(it.animatedValue as Int)
            }
            start()
        }
    }

    private fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
