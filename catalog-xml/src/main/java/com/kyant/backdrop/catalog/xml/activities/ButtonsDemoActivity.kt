package com.kyant.backdrop.catalog.xml.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivityButtonsDemoBinding

/**
 * Demonstrates liquid glass buttons with LayerBackdropView
 */
class ButtonsDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityButtonsDemoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityButtonsDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Liquid Glass Buttons"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupButtons() {
        // Get the backdrop from LayerBackdropView
        val backdrop = binding.backdropLayer.getBackdrop()
        
        // Transparent Liquid Button
        binding.transparentButton.apply {
            setBackdropSource(backdrop)
            buttonText.text = "Transparent Liquid Button"
            buttonText.setTextColor(Color.BLACK)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Transparent button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Surface Liquid Button
        binding.surfaceButton.apply {
            setBackdropSource(backdrop)
            surfaceColor = Color.argb(77, 255, 255, 255) // White 0.3 alpha
            buttonText.text = "Surface Liquid Button"
            buttonText.setTextColor(Color.BLACK)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Surface button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Tinted Liquid Button (Blue)
        binding.tintedBlueButton.apply {
            setBackdropSource(backdrop)
            tintColor = Color.parseColor("#0088FF")
            buttonText.text = "Tinted Liquid Button"
            buttonText.setTextColor(Color.WHITE)
            setOnClickListener {
                Toast.makeText(this@ButtonsDemoActivity, "Blue tinted button clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Tinted Liquid Button (Orange)
        binding.tintedOrangeButton.apply {
            setBackdropSource(backdrop)
            tintColor = Color.parseColor("#FF8D28")
            buttonText.text = "Tinted Liquid Button"
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
