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

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.components.LiquidButton

/**
 * Buttons activity - matches Compose ButtonsContent exactly
 * Displays 4 button variations:
 * 1. Transparent Liquid Button
 * 2. Surface Liquid Button (white surface with 30% opacity)
 * 3. Blue Tinted Liquid Button (#0088FF)
 * 4. Orange Tinted Liquid Button (#FF8D28)
 */
class ButtonsActivity : AppCompatActivity() {

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
        
        // 1. Transparent Liquid Button
        val button1 = LiquidButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * density).toInt()
            }
            setText("Transparent Liquid Button")
            setTextColor(Color.BLACK)
        }
        mainLayout.addView(button1)
        
        // 2. Surface Liquid Button (white surface with 30% opacity)
        val button2 = LiquidButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * density).toInt()
            }
            setText("Surface Liquid Button")
            setTextColor(Color.BLACK)
            surfaceColor = Color.argb(77, 255, 255, 255) // White with 30% opacity (0.3 * 255 = 77)
        }
        mainLayout.addView(button2)
        
        // 3. Blue Tinted Liquid Button (#0088FF)
        val button3 = LiquidButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * density).toInt()
            }
            setText("Tinted Liquid Button")
            setTextColor(Color.WHITE)
            tintColor = Color.parseColor("#0088FF")
        }
        mainLayout.addView(button3)
        
        // 4. Orange Tinted Liquid Button (#FF8D28)
        val button4 = LiquidButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText("Tinted Liquid Button")
            setTextColor(Color.WHITE)
            tintColor = Color.parseColor("#FF8D28")
        }
        mainLayout.addView(button4)
        
        setContentView(mainLayout)
    }
}