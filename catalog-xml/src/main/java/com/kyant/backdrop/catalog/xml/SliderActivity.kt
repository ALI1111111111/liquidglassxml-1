package com.kyant.backdrop.catalog.xml

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.components.LiquidSlider

/**
 * Slider activity - matches Compose SliderContent exactly
 * Displays a single slider with value range 0-100
 */
class SliderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set wallpaper background
        window.decorView.setBackgroundResource(R.drawable.wallpaper)
        
        // Create main layout
        val density = resources.displayMetrics.density
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                (32 * density).toInt(),
                (48 * density).toInt(),
                (32 * density).toInt(),
                (48 * density).toInt()
            )
        }
        
        // Value display text
        val valueText = TextView(this).apply {
            text = "50"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (32 * density).toInt())
        }
        mainLayout.addView(valueText)
        
        // Slider with range 0-100, initial value 50
        val slider = LiquidSlider(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setValueRange(0f, 100f)
            setValue(50f, false)
            
            setOnValueChangeListener { value ->
                valueText.text = value.toInt().toString()
            }
        }
        mainLayout.addView(slider)
        
        setContentView(mainLayout)
    }
}