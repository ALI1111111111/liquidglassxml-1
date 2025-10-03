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
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import com.kyant.backdrop.xml.effects.*

/**
 * Buttons activity demonstrating various button styles with liquid glass effects using XML layout.
 * Equivalent to the Buttons destination in the original Compose catalog.
 */
class ButtonsActivity : AppCompatActivity() {

    private lateinit var rootLayout: CoordinatorLayout

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
        setContentView(R.layout.activity_buttons)

        rootLayout = findViewById(R.id.rootLayout)

        setupToolbar()
        setupVibrancyEffects() // Critical for liquid glass visibility
        setupButtonListeners()
        setupBackgroundPicker()
    }

    private fun setupVibrancyEffects() {
        // Apply vibrancy effect to all glass containers - this is essential for visibility
        findViewById<LiquidGlassContainer>(R.id.primaryGlassContainer).apply {
            setColorFilterEffect(ColorFilterEffect.vibrant())
        }

        findViewById<LiquidGlassContainer>(R.id.secondaryGlassContainer).apply {
            setColorFilterEffect(ColorFilterEffect.vibrant())
        }

        findViewById<LiquidGlassContainer>(R.id.accentGlassContainer).apply {
            setColorFilterEffect(ColorFilterEffect.vibrant())
        }

        findViewById<LiquidGlassContainer>(R.id.iconGlassContainer).apply {
            setColorFilterEffect(ColorFilterEffect.vibrant())
        }
    }

    private fun setupToolbar() {
        supportActionBar?.title = "Buttons"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.primaryButton).setOnClickListener {
            Toast.makeText(this, "Primary button clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.secondaryButton).setOnClickListener {
            Toast.makeText(this, "Secondary button clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.accentButton).setOnClickListener {
            Toast.makeText(this, "Accent button clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.iconButton).setOnClickListener {
            Toast.makeText(this, "Icon button clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupBackgroundPicker() {
        findViewById<FloatingActionButton>(R.id.fabBackgroundPicker).setOnClickListener {
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
                        rootLayout.background = resource
                        Toast.makeText(this@ButtonsActivity, "Background changed! Glass effects updated.", Toast.LENGTH_SHORT).show()
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