package com.kyant.backdrop.catalog.xml.destinations

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.kyant.backdrop.catalog.xml.databinding.FragmentControlCenterBinding
import com.kyant.backdrop.xml.BackdropEffect
import com.kyant.backdrop.xml.HighlightType
import com.kyant.backdrop.xml.LiquidGlassView
import kotlin.math.abs
import kotlin.math.atan2

class ControlCenterFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentControlCenterBinding? = null
    private val binding get() = _binding!!

    private var initialY = 0f
    private var initialTranslationY = 0f

    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlCenterBinding.inflate(inflater, container, false)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGestureHandling()
        startEnterAnimation()
    }

    private fun startEnterAnimation() {
        binding.root.translationY = 400f
        binding.root.alpha = 0f
        binding.root.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .start()

        updateBackdropEffects(0f)
    }

    private fun setupGestureHandling() {
        binding.root.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialTranslationY = binding.root.translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    val newTranslationY = initialTranslationY + deltaY
                    binding.root.translationY = newTranslationY
                    updateBackdropEffects(newTranslationY)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(binding.root.translationY) > view.height / 4) {
                        // Animate out
                        binding.root.animate()
                            .translationY(if (binding.root.translationY > 0) view.height.toFloat() else -view.height.toFloat())
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                parentFragmentManager.popBackStack()
                            }
                            .start()
                    } else {
                        // Animate back to center
                        binding.root.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                        updateBackdropEffects(0f, isAnimating = true)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateBackdropEffects(translationY: Float, isAnimating: Boolean = false) {
        val maxTranslation = binding.root.height / 2f
        val progress = (abs(translationY) / maxTranslation).coerceIn(0f, 1f)

        val blurRadius = 24f * progress
        val refractionAmount = 48f * (1 - progress)

        val effects = listOf(
            BackdropEffect.Blur(blurRadius, blurRadius),
            BackdropEffect.Vibrancy
        )

        // Apply parallax effect
        binding.connectivityGroup.translationY = translationY * 0.5f
        binding.mediaGroup.translationY = translationY * 0.5f
        binding.slidersGroup.translationY = translationY * 0.8f
        binding.buttonsGroup.translationY = translationY * 0.8f

        (binding.root as ViewGroup).forEach { view ->
            if (view is LiquidGlassView) {
                if (isAnimating) {
                    // Use animation for a smoother transition back
                    view.animationDuration = 300
                } else {
                    view.animationDuration = 0
                }

                view.setBackdropEffects(effects)
                view.setRefraction(
                    height = 32f,
                    amount = refractionAmount,
                    depthEffect = 0.2f
                )
                view.alpha = 1 - progress
                view.scaleX = 1 - (progress * 0.05f)
                view.scaleY = 1 - (progress * 0.05f)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            val angle = atan2(event.values[0], event.values[1])
            (binding.root as ViewGroup).forEach { view ->
                if (view is LiquidGlassView) {
                    view.setHighlight(angle, 1.5f, HighlightType.SPECULAR)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
