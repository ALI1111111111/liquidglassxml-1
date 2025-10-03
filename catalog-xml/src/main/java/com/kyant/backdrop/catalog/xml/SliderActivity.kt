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

package com.kyant.backdrop.catalog.xml

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kyant.backdrop.catalog.xml.components.LiquidSlider
import kotlin.math.roundToInt

class SliderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set wallpaper background
        window.decorView.setBackgroundResource(R.drawable.wallpaper)
        
        // Create main layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (24 * resources.displayMetrics.density).toInt(),
                (48 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt()
            )
        }
        
        // Add title
        val titleText = TextView(this).apply {
            text = "Liquid Sliders"
            textSize = 28f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, (32 * resources.displayMetrics.density).toInt())
        }
        mainLayout.addView(titleText)
        
        // Create sliders exactly matching Compose catalog
        createSliders(mainLayout)
        
        setContentView(mainLayout)
    }
    
    private fun createSliders(parent: LinearLayout) {
        val density = resources.displayMetrics.density
        val sliderMargin = (24 * density).toInt()
        
        // 1. Default slider
        createSliderWithLabel(parent, "Default", 0.5f) { slider ->
            // No special styling - uses default appearance
        }
        
        // 2. Blue tinted slider
        createSliderWithLabel(parent, "Blue Tint", 0.3f) { slider ->
            slider.tintColor = Color.rgb(0, 122, 255)
        }
        
        // 3. Orange surface slider
        createSliderWithLabel(parent, "Orange Surface", 0.7f) { slider ->
            slider.surfaceColor = Color.rgb(255, 149, 0)
        }
        
        // 4. Red tinted slider
        createSliderWithLabel(parent, "Red Tint", 0.8f) { slider ->
            slider.tintColor = Color.rgb(255, 59, 48)
        }
        
        // 5. Green surface slider
        createSliderWithLabel(parent, "Green Surface", 0.2f) { slider ->
            slider.surfaceColor = Color.rgb(52, 199, 89)
        }
        
        // 6. Custom range slider (0-100)
        createSliderWithLabel(parent, "Custom Range (0-100)", 75f) { slider ->
            slider.setValueRange(0f, 100f)
            slider.setValue(75f)
            slider.tintColor = Color.rgb(175, 82, 222)
        }
    }
    
    private fun createSliderWithLabel(
        parent: LinearLayout,
        label: String,
        initialValue: Float,
        configurator: (LiquidSlider) -> Unit
    ) {
        val density = resources.displayMetrics.density
        
        // Create container for this slider
        val sliderContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, (24 * density).toInt())
        }
        
        // Create label
        val labelText = TextView(this).apply {
            text = label
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.8f
            setPadding(0, 0, 0, (8 * density).toInt())
        }
        sliderContainer.addView(labelText)
        
        // Create value display
        val valueText = TextView(this).apply {
            text = formatSliderValue(initialValue)
            textSize = 14f
            setTextColor(Color.WHITE)
            alpha = 0.6f
            setPadding(0, 0, 0, (12 * density).toInt())
        }
        sliderContainer.addView(valueText)
        
        // Create slider
        val slider = LiquidSlider(this).apply {
            setValue(initialValue, false)
            
            // Update value display when slider changes
            setOnValueChangeListener { value ->
                valueText.text = formatSliderValue(value)
            }
        }
        
        // Apply custom configuration
        configurator(slider)
        
        // Set layout params for slider
        val sliderLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        slider.layoutParams = sliderLayoutParams
        
        sliderContainer.addView(slider)
        parent.addView(sliderContainer)
    }
    
    private fun formatSliderValue(value: Float): String {
        return if (value >= 10f) {
            // For custom range slider (0-100)
            "${value.roundToInt()}"
        } else {
            // For standard 0-1 range
            "%.2f".format(value)
        }
    }
}