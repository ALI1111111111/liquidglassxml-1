package com.kyant.backdrop.catalog.xml.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivitySliderDemoBinding

/**
 * Demonstrates liquid glass sliders with LayerBackdropView
 */
class SliderDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySliderDemoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySliderDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSliders()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Liquid Glass Sliders"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupSliders() {
        // Get the backdrop from LayerBackdropView
        val backdrop = binding.backdropLayer.getBackdrop()
        
        // Volume Slider
        binding.volumeSlider.apply {
            setBackdropSource(backdrop)
            setValueRange(0f, 100f)
            setValue(50f, false)
            tintColor = Color.parseColor("#0088FF")
            setOnValueChangeListener { value ->
                binding.volumeValue.text = "${value.toInt()}%"
            }
        }
        
        // Brightness Slider
        binding.brightnessSlider.apply {
            setBackdropSource(backdrop)
            setValueRange(0f, 100f)
            setValue(75f, false)
            tintColor = Color.parseColor("#FFC107")
            setOnValueChangeListener { value ->
                binding.brightnessValue.text = "${value.toInt()}%"
            }
        }
        
        // Temperature Slider
        binding.temperatureSlider.apply {
            setBackdropSource(backdrop)
            setValueRange(15f, 30f)
            setValue(20f, false)
            tintColor = Color.parseColor("#FF5722")
            setOnValueChangeListener { value ->
                binding.temperatureValue.text = "${value.toInt()}°C"
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
