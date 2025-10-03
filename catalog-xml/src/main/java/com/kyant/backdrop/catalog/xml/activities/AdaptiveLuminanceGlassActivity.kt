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

import android.animation.ValueAnimator
import android.graphics.*
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassView
import kotlin.math.max
import kotlin.math.min

class AdaptiveLuminanceGlassActivity : AppCompatActivity() {

    private lateinit var glassView: LiquidGlassView
    private lateinit var contentText: TextView
    
    private var currentScale = 1f
    private var currentRotation = 0f
    private var currentTranslationX = 0f
    private var currentTranslationY = 0f
    
    private var luminanceValue = 0.5f
    private var luminanceAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set wallpaper background  
        window.decorView.setBackgroundResource(R.drawable.wallpaper)
        
        // Create main layout
        val mainLayout = FrameLayout(this)
        
        // Create glass view
        val density = resources.displayMetrics.density
        glassView = LiquidGlassView(this).apply {
            setCornerRadius(24 * density)
            
            // Initial effects
            setRefractionEffect(
                RefractionEffect(
                    height = 24 * density,
                    amount = 48 * density,
                    hasDepthEffect = true
                )
            )
            setBlurEffect(BlurEffect(8 * density))
            setColorFilterEffect(ColorFilterEffect(brightness = 0.1f, saturation = 1.5f))
        }
        
        // Content text
        contentText = TextView(this).apply {
            text = "Adaptive Glass\n\nPinch to zoom\nDrag to move\nRotate with two fingers\n\nGlass adapts to background luminance"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding(
                (32 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt()
            )
        }
        
        glassView.addView(contentText)
        
        // Position glass view in center
        val glassParams = FrameLayout.LayoutParams(
            (300 * density).toInt(),
            (300 * density).toInt()
        ).apply {
            gravity = android.view.Gravity.CENTER
        }
        
        mainLayout.addView(glassView, glassParams)
        
        // Setup gesture detection
        setupGestureDetection(glassView)
        
        // Start luminance adaptation simulation
        startLuminanceAdaptation()
        
        setContentView(mainLayout)
    }
    
    private fun setupGestureDetection(view: View) {
        val scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                currentScale *= detector.scaleFactor
                currentScale = max(0.5f, min(currentScale, 3f))
                updateTransform()
                return true
            }
        })
        
        var lastX = 0f
        var lastY = 0f
        var initialRotation = 0f
        var lastAngle = 0f
        var isRotating = false
        
        view.setOnTouchListener { v, event ->
            scaleDetector.onTouchEvent(event)
            
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    isRotating = false
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        isRotating = true
                        initialRotation = currentRotation
                        lastAngle = calculateAngle(event)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 2 && isRotating) {
                        // Rotation
                        val currentAngle = calculateAngle(event)
                        val deltaAngle = currentAngle - lastAngle
                        currentRotation = initialRotation + deltaAngle
                        updateTransform()
                    } else if (event.pointerCount == 1 && !isRotating) {
                        // Translation
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        currentTranslationX += deltaX
                        currentTranslationY += deltaY
                        lastX = event.rawX
                        lastY = event.rawY
                        updateTransform()
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    if (event.pointerCount == 2) {
                        isRotating = false
                    }
                }
            }
            true
        }
    }
    
    private fun calculateAngle(event: MotionEvent): Float {
        val deltaX = event.getX(1) - event.getX(0)
        val deltaY = event.getY(1) - event.getY(0)
        return Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
    }
    
    private fun updateTransform() {
        glassView.apply {
            scaleX = currentScale
            scaleY = currentScale
            rotation = currentRotation
            translationX = currentTranslationX
            translationY = currentTranslationY
        }
    }
    
    private fun startLuminanceAdaptation() {
        // Simulate adaptive luminance changes over time
        luminanceAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            
            addUpdateListener { animation ->
                luminanceValue = animation.animatedValue as Float
                updateGlassEffects(luminanceValue)
                updateContentColor(luminanceValue)
            }
            
            start()
        }
    }
    
    private fun updateGlassEffects(luminance: Float) {
        val density = resources.displayMetrics.density
        
        // Adjust effects based on luminance
        val brightness = if (luminance > 0.5f) {
            // Bright background - increase brightness
            (luminance - 0.5f) * 2f * 0.4f + 0.1f
        } else {
            // Dark background - decrease brightness
            (luminance - 0.5f) * 2f * 0.3f + 0.1f
        }
        
        val contrast = if (luminance > 0.5f) {
            // Bright background - reduce contrast
            1f - (luminance - 0.5f) * 2f
        } else {
            1f
        }
        
        val blurAmount = if (luminance > 0.5f) {
            // Bright background - more blur
            8 * density + (luminance - 0.5f) * 2f * 8 * density
        } else {
            // Dark background - less blur
            8 * density - (0.5f - luminance) * 2f * 6 * density
        }
        
        glassView.setColorFilterEffect(
            ColorFilterEffect(
                brightness = brightness,
                contrast = contrast,
                saturation = 1.5f
            )
        )
        
        glassView.setBlurEffect(BlurEffect(blurAmount.coerceIn(2 * density, 16 * density)))
    }
    
    private fun updateContentColor(luminance: Float) {
        // Adapt text color based on background luminance
        val textColor = if (luminance > 0.5f) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        contentText.setTextColor(textColor)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        luminanceAnimator?.cancel()
    }
}
