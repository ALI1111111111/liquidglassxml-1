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
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.databinding.ActivityLiquidTestBinding
import com.kyant.backdrop.xml.effects.*

/**
 * Test activity to verify liquid glass effects are working with very obvious visual effects.
 */
class LiquidTestActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLiquidTestBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiquidTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupLiquidGlassTests()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Liquid Glass Test"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupLiquidGlassTests() {
        // Get the wallpaper drawable as backdrop source
        val backgroundDrawable = resources.getDrawable(com.kyant.backdrop.catalog.xml.R.drawable.wallpaper, theme)
        
        // Test 1: Very strong refraction effect
//        binding.strongRefractionContainer.setBackgroundSource(backgroundDrawable)
//        binding.strongRefractionContainer.setCornerRadius(24f)
//        binding.strongRefractionContainer.setRefractionEffect(RefractionEffect(
//            height = 10f,      // Very high refraction
//            amount = 50f,       // Strong distortion
//            hasDepthEffect = false
//        ))
        
        // Test 2: Very strong blur effect
        binding.strongBlurContainer.setBackgroundSource(backgroundDrawable)
        binding.strongBlurContainer.setCornerRadius(24f)
        binding.strongBlurContainer.setBlurEffect(BlurEffect(
            radius = 50f        // Very strong blur
        ))
        
        // Test 3: Strong chromatic dispersion
        binding.chromaticContainer.setBackgroundSource(backgroundDrawable)
        binding.chromaticContainer.setCornerRadius(24f)
        binding.chromaticContainer.setDispersionEffect(DispersionEffect(
            height = 80f,       // Very high dispersion
            amount = 40f        // Strong rainbow effect
        ))
        
        // Also add some highlight effects for visibility
        val strongHighlight = HighlightEffect(
            angle = 6.2f,
            alpha = 0.8f
        )
        
        binding.strongRefractionContainer.setHighlightEffect(strongHighlight)
        binding.strongBlurContainer.setHighlightEffect(strongHighlight)
        binding.chromaticContainer.setHighlightEffect(strongHighlight)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}