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
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.databinding.ActivityGlassPlaygroundBinding
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import com.kyant.backdrop.xml.effects.*

/**
 * Glass Playground activity demonstrating LiquidGlassContainer with customizable parameters.
 * Equivalent to the Glass Playground destination in the original Compose catalog.
 */
class GlassPlaygroundActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGlassPlaygroundBinding
    private lateinit var liquidGlassView: LiquidGlassContainer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlassPlaygroundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupLiquidGlass()
        setupControls()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Glass Playground"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupLiquidGlass() {
        liquidGlassView = binding.liquidGlassView
        
        // Set initial glass effects with default parameters
        liquidGlassView.setRefractionEffect(RefractionEffect(
            height = 15f,
            amount = 0.2f
        ))
        liquidGlassView.setBlurEffect(BlurEffect(
            radius = 8f
        ))
        liquidGlassView.setDispersionEffect(DispersionEffect(
            height = 8f,
            amount = 0.4f
        ))
    }
    
    private fun setupControls() {
        // Noise control
        binding.noiseSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val noise = progress / 100f
                    binding.noiseValue.text = String.format("%.2f", noise)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Noise Scale control
        binding.noiseScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val noiseScale = progress + 50f // 50-550 range
                    binding.noiseScaleValue.text = String.format("%.0f", noiseScale)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Chromatic Aberration control
        binding.chromaticAberrationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val chromaticAberration = progress / 100f
                    binding.chromaticAberrationValue.text = String.format("%.2f", chromaticAberration)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Blur control
        binding.blurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val blur = progress / 100f
                    binding.blurValue.text = String.format("%.2f", blur)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Distortion control
        binding.distortionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val distortion = progress / 100f
                    binding.distortionValue.text = String.format("%.2f", distortion)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Distortion Scale control
        binding.distortionScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val distortionScale = progress + 50f // 50-550 range
                    binding.distortionScaleValue.text = String.format("%.0f", distortionScale)
                    updateEffect()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Set initial values
        binding.noiseSeekBar.progress = 50 // 0.5f
        binding.noiseScaleSeekBar.progress = 100 // 150f
        binding.chromaticAberrationSeekBar.progress = 40 // 0.4f
        binding.blurSeekBar.progress = 10 // 0.1f
        binding.distortionSeekBar.progress = 20 // 0.2f
        binding.distortionScaleSeekBar.progress = 150 // 200f
        
        updateValueLabels()
    }
    
    private fun updateValueLabels() {
        binding.noiseValue.text = String.format("%.1f", (binding.noiseSeekBar.progress + 50f) / 10f)
        binding.noiseScaleValue.text = String.format("%.2f", binding.noiseScaleSeekBar.progress / 100f)
        binding.chromaticAberrationValue.text = String.format("%.2f", binding.chromaticAberrationSeekBar.progress / 100f)
        binding.blurValue.text = String.format("%.1f", binding.blurSeekBar.progress / 5f)
        binding.distortionValue.text = String.format("%.2f", binding.distortionSeekBar.progress / 100f)
        binding.distortionScaleValue.text = String.format("%.1f", binding.distortionScaleSeekBar.progress / 20f)
    }
    
    private fun updateEffect() {
        val refractionHeight = binding.noiseSeekBar.progress + 50f / 10f // 5-15 range
        val refractionAmount = binding.noiseScaleSeekBar.progress / 100f
        val dispersionIntensity = binding.chromaticAberrationSeekBar.progress / 100f
        val blurRadius = binding.blurSeekBar.progress / 5f // 0-20 range
        val highlightIntensity = binding.distortionSeekBar.progress / 100f
        val shadowOffset = binding.distortionScaleSeekBar.progress / 20f // 0-5 range
        
        liquidGlassView.setRefractionEffect(RefractionEffect(
            height = refractionHeight,
            amount = refractionAmount
        ))
        liquidGlassView.setDispersionEffect(DispersionEffect(
            height = 8f,
            amount = dispersionIntensity
        ))
        liquidGlassView.setBlurEffect(BlurEffect(
            radius = blurRadius
        ))
        liquidGlassView.setHighlightEffect(HighlightEffect(
            angle = 1.25f,
            alpha = highlightIntensity
        ))
        liquidGlassView.setShadowEffect(ShadowEffect(
            offsetX = shadowOffset,
            offsetY = shadowOffset,
            radius = shadowOffset * 2,
            color = android.graphics.Color.BLACK,
            alpha = 0.3f
        ))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}