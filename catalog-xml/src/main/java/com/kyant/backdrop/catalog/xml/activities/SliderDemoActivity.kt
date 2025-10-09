package com.kyant.backdrop.catalog.xml.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivitySliderDemoBinding

/**
 * Demonstrates liquid glass sliders with LayerBackdropView
 * Includes real-time backdrop updates with image picker
 */
class SliderDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySliderDemoBinding
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadBackgroundImage(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySliderDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSliders()
        setupImagePicker()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Liquid Glass Sliders"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupImagePicker() {
        binding.pickImageButton.setOnClickListener {
            openImagePicker()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }
    
    private fun loadBackgroundImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Set the bitmap to the backdrop layer - real-time updates!
            binding.backdropLayer.setBackgroundImage(bitmap)
            
            Toast.makeText(this, "Background updated! Slider glass effects update in real-time.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupSliders() {
        // Pass the LayerBackdropView directly to enable real-time backdrop updates during drag
        
        // Volume Slider
        binding.volumeSlider.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            setValueRange(0f, 100f)
            setValue(50f, false)
            tintColor = Color.parseColor("#0088FF")
            setOnValueChangeListener { value ->
                binding.volumeValue.text = "${value.toInt()}%"
            }
        }
        
        // Brightness Slider
        binding.brightnessSlider.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            setValueRange(0f, 100f)
            setValue(75f, false)
            tintColor = Color.parseColor("#FFC107")
            setOnValueChangeListener { value ->
                binding.brightnessValue.text = "${value.toInt()}%"
            }
        }
        
        // Temperature Slider
        binding.temperatureSlider.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
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
