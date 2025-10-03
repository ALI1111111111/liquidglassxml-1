/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyant.backdrop.catalog.xml.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivityLockScreenBinding
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.presets.LiquidGlassPresets
import java.text.SimpleDateFormat
import java.util.*

/**
 * Lock Screen activity demonstrating iOS-style lock screen interface.
 * Equivalent to the Lock Screen destination in the original Compose catalog.
 */
class LockScreenActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLockScreenBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupLockScreen()
        updateTime()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Lock Screen"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupLockScreen() {
        // Setup time and date display container with glass effect
        LiquidGlassPresets.subtleGlassPanel(24f).applyTo(binding.timeContainer)
        
        // Setup quick actions with glass effects
        LiquidGlassPresets.iosGlassButton(16f).applyTo(binding.flashlightContainer)
        LiquidGlassPresets.iosGlassButton(16f).applyTo(binding.cameraContainer)
        
        // Setup control center panel with blur effect
        binding.controlCenterPanel.setBlurEffect(BlurEffect(
            radius = 20f
        ))
        binding.controlCenterPanel.setHighlightEffect(HighlightEffect(
            angle = 1.25f,
            alpha = 0.15f
        ))
        
        // Setup notification panel with glass effect
        binding.notificationPanel.setBlurEffect(BlurEffect(
            radius = 15f
        ))
        binding.notificationPanel.setInnerShadowEffect(InnerShadowEffect(
            offsetX = 0f,
            offsetY = 2f,
            radius = 4f,
            color = android.graphics.Color.BLACK,
            alpha = 0.2f
        ))
        
        // Setup unlock slider with refraction effect
        binding.unlockSlider.setRefractionEffect(RefractionEffect(
            height = 12f,
            amount = 0.25f
        ))
        binding.unlockSlider.setDispersionEffect(DispersionEffect(
            height = 8f,
            amount = 0.3f
        ))
        
        // Setup click listeners
        binding.flashlightButton.setOnClickListener {
            Toast.makeText(this, "Flashlight toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.cameraButton.setOnClickListener {
            Toast.makeText(this, "Camera opened", Toast.LENGTH_SHORT).show()
        }
        
        binding.unlockButton.setOnClickListener {
            Toast.makeText(this, "Unlocking device...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val currentTime = Date()
        
        binding.timeText.text = timeFormat.format(currentTime)
        binding.dateText.text = dateFormat.format(currentTime)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}