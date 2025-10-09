package com.kyant.backdrop.catalog.xml.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivityButtonsDemoBinding

/**
 * Demonstrates liquid glass buttons with LayerBackdropView
 * Now includes image picker for dynamic background changes
 */
class ButtonsDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityButtonsDemoBinding
    
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
        binding = ActivityButtonsDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupImagePicker()
        setupButtons()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Liquid Glass Buttons"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupImagePicker() {
        // Create image picker button programmatically
        val pickImageButton = Button(this).apply {
            text = "Change Background Image"
            setBackgroundColor(Color.parseColor("#0088FF"))
            setTextColor(Color.WHITE)
            setPadding(32, 24, 32, 24)
            setOnClickListener {
                openImagePicker()
            }
        }
        
        // Add to layout (you'll need to add this to your XML layout if needed)
        // For now, we can trigger it from one of the buttons
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
            
            // Set the bitmap to the backdrop layer
            binding.backdropLayer.setBackgroundImage(bitmap)
            
            Toast.makeText(this, "Background image updated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupButtons() {
        // Pass the LayerBackdropView directly to enable real-time backdrop updates during touch
        
        // Wire up image picker button
        binding.pickImageButton.setOnClickListener {
            openImagePicker()
        }
        
        // Transparent Liquid Button
        binding.transparentButton.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            buttonText.text = "Transparent Liquid Button"
            buttonText.setTextColor(Color.BLACK)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Transparent button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Surface Liquid Button
        binding.surfaceButton.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            surfaceColor = Color.argb(77, 255, 255, 255) // White 0.3 alpha
            buttonText.text = "Surface Liquid Button"
            buttonText.setTextColor(Color.BLACK)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Surface button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Tinted Liquid Button (Blue)
        binding.tintedBlueButton.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            tintColor = Color.parseColor("#0088FF")
            buttonText.text = "Tinted Liquid Button (Blue)"
            buttonText.setTextColor(Color.WHITE)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Blue button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Tinted Liquid Button (Orange)
        binding.tintedOrangeButton.apply {
            setBackdropSource(binding.backdropLayer)  // Pass the view, not just the backdrop!
            tintColor = Color.parseColor("#FF8D28")
            buttonText.text = "Tinted Liquid Button (Orange)"
            buttonText.setTextColor(Color.WHITE)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Orange tinted button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
