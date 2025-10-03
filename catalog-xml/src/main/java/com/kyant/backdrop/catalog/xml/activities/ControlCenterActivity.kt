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

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kyant.backdrop.catalog.xml.databinding.ActivityControlCenterBinding
import com.kyant.backdrop.xml.effects.*

/**
 * Control Center activity demonstrating glass control panel interface.
 * Equivalent to the Control Center destination in the original Compose catalog.
 */
class ControlCenterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityControlCenterBinding
    
    // Media picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { changeBackground(it) }
    }
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupVibrancyEffects()
        setupClickListeners()
        setupBackgroundPicker()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Control Center"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupVibrancyEffects() {
        // Add vibrancy effects to all glass containers for proper visibility
        binding.connectivityPanel.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.mediaPanel.setColorFilterEffect(ColorFilterEffect.vibrant())
        
        // Individual connectivity buttons
        binding.wifiContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.bluetoothContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.airplaneContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.cellularContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        
        // Utility controls
        binding.brightnessContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.volumeContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        
        // Action buttons
        binding.flashlightContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.timerContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.calculatorContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        binding.cameraContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
    }
    
    private fun setupClickListeners() {
        // Connectivity toggles
        binding.wifiButton.setOnClickListener {
            Toast.makeText(this, "Wi-Fi toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.bluetoothButton.setOnClickListener {
            Toast.makeText(this, "Bluetooth toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.airplaneButton.setOnClickListener {
            Toast.makeText(this, "Airplane mode toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.cellularButton.setOnClickListener {
            Toast.makeText(this, "Cellular data toggled", Toast.LENGTH_SHORT).show()
        }
        
        // Media controls
        binding.playPauseButton.setOnClickListener {
            Toast.makeText(this, "Play/Pause", Toast.LENGTH_SHORT).show()
        }
        
        binding.previousButton.setOnClickListener {
            Toast.makeText(this, "Previous track", Toast.LENGTH_SHORT).show()
        }
        
        binding.nextButton.setOnClickListener {
            Toast.makeText(this, "Next track", Toast.LENGTH_SHORT).show()
        }
        
        // Action buttons
        binding.flashlightButton.setOnClickListener {
            Toast.makeText(this, "Flashlight toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.timerButton.setOnClickListener {
            Toast.makeText(this, "Timer opened", Toast.LENGTH_SHORT).show()
        }
        
        binding.calculatorButton.setOnClickListener {
            Toast.makeText(this, "Calculator opened", Toast.LENGTH_SHORT).show()
        }
        
        binding.cameraButton.setOnClickListener {
            Toast.makeText(this, "Camera opened", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun setupBackgroundPicker() {
        binding.fabBackgroundPicker.setOnClickListener {
            checkPermissionAndOpenPicker()
        }
    }
    
    private fun checkPermissionAndOpenPicker() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                Toast.makeText(this, "Permission needed to select background images", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(permission)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun openImagePicker() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun changeBackground(imageUri: Uri) {
        try {
            Glide.with(this)
                .load(imageUri)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        binding.rootLayout.background = resource
                        Toast.makeText(this@ControlCenterActivity, "Background changed! Glass effects updated.", Toast.LENGTH_SHORT).show()
                    }
                    
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle cleanup if needed
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}