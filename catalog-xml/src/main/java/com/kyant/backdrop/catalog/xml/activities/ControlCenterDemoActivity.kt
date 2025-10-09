package com.kyant.backdrop.catalog.xml.activities

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.kyant.backdrop.catalog.xml.databinding.ActivityControlCenterDemoBinding
import com.kyant.backdrop.xml.effects.*
import kotlin.math.abs

/**
 * Control Center demo matching Compose ControlCenterContent
 * Features:
 * - Drag-to-dismiss gesture with spring animations
 * - Glass cards with vibrancy and refraction effects
 * - Dynamic enter/exit animations
 * - Matching Compose layout and spacing
 */
class ControlCenterDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityControlCenterDemoBinding
    private lateinit var gestureDetector: GestureDetectorCompat
    
    // Animation properties
    private var panelTranslationY = 0f
    private var panelAlpha = 1f
    private var isDragging = false
    private val maxDragHeight = 1000f
    
    // Spring animations
    private var translationSpring: SpringAnimation? = null
    private var alphaAnimator: ValueAnimator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlCenterDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupGlassCards()
        setupDragGesture()
        
        // Initial enter animation
        animateEnter()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Control Center"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupGlassCards() {
        val backdrop = binding.backdropLayer.getBackdrop()
        val density = resources.displayMetrics.density
        
        // Glass effect matching Compose: vibrancy() + refraction(24dp, 48dp, true)
        val glassEffect = com.kyant.backdrop.xml.utils.LiquidGlassEffectBuilder().apply {
            cornerRadius(34f * density) // itemSize / 2 = 68dp / 2 = 34dp
            blur(0.5f * density)
            refraction(24f * density, 48f * density, true)
            ambientHighlight(0.15f)
        }
        
        val containerColor = Color.argb(13, 0, 0, 0) // Black 0.05 alpha
        
        // Apply to all glass cards
        listOf(
            binding.gridCard,
            binding.displayCard,
            binding.smallCard1,
            binding.smallCard2,
            binding.wideCard,
            binding.tallCard,
            binding.tallCard2
        ).forEach { card ->
            card.setBackgroundSource(backdrop)
            glassEffect.applyTo(card)
            
            // Add surface tint
            card.setColorFilterEffect(ColorFilterEffect.vibrant())
        }
        
        // Setup button clicks
        setupButtonClicks()
    }
    
    private fun setupButtonClicks() {
        binding.wifiButton.setOnClickListener {
            Toast.makeText(this, "WiFi toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.bluetoothButton.setOnClickListener {
            Toast.makeText(this, "Bluetooth toggled", Toast.LENGTH_SHORT).show()
        }
        
        binding.airplaneButton.setOnClickListener {
            Toast.makeText(this, "Airplane mode toggled", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupDragGesture() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!isDragging && e1 != null) {
                    isDragging = true
                }
                
                // Update translation based on drag
                panelTranslationY -= distanceY
                panelTranslationY = panelTranslationY.coerceIn(-maxDragHeight, maxDragHeight)
                
                // Update alpha based on position (fade out when dragging up)
                val progress = (panelTranslationY / maxDragHeight).coerceIn(-1f, 1f)
                panelAlpha = (1f - abs(progress)).coerceIn(0f, 1f)
                
                updatePanelState()
                updateBackgroundBlur(panelAlpha)
                
                return true
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                isDragging = false
                
                // Determine target based on velocity and position
                val shouldDismiss = velocityY < -1000f || panelTranslationY < -maxDragHeight / 2
                
                if (shouldDismiss) {
                    animateExit()
                } else {
                    animateToOriginal(velocityY)
                }
                
                return true
            }
        })
        
        binding.controlPanel.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    translationSpring?.cancel()
                    alphaAnimator?.cancel()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        isDragging = false
                        // Snap back if not flung
                        val shouldDismiss = panelTranslationY < -maxDragHeight / 2
                        if (shouldDismiss) {
                            animateExit()
                        } else {
                            animateToOriginal(0f)
                        }
                    }
                    false
                }
                else -> gestureDetector.onTouchEvent(event)
            }
        }
    }
    
    private fun updatePanelState() {
        binding.controlPanel.apply {
            translationY = panelTranslationY
            alpha = panelAlpha
            
            // Scale effect when dragging
            val progress = abs(panelTranslationY / maxDragHeight).coerceIn(0f, 1f)
            scaleX = 1f - (0.1f * progress)
            scaleY = 1f + (0.1f * progress)
        }
    }
    
    private fun updateBackgroundBlur(alpha: Float) {
        // Apply blur to background based on panel visibility
        // This matches Compose version's drawWithContent blur effect
        val blurRadius = 4f * resources.displayMetrics.density * alpha
        binding.rootLayout.setBackgroundColor(Color.argb((alpha * 102).toInt(), 0, 0, 0)) // dimColor
    }
    
    private fun animateEnter() {
        // Initial state: off-screen and invisible
        binding.controlPanel.apply {
            translationY = -48f * resources.displayMetrics.density
            alpha = 0f
        }
        
        // Animate to visible
        translationSpring = SpringAnimation(binding.controlPanel, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring = SpringForce(0f).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_MEDIUM
            }
            start()
        }
        
        alphaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener {
                val alpha = it.animatedValue as Float
                binding.controlPanel.alpha = alpha
                updateBackgroundBlur(alpha)
            }
            start()
        }
    }
    
    private fun animateToOriginal(velocity: Float) {
        // Spring back to original position
        translationSpring = SpringAnimation(binding.controlPanel, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring = SpringForce(0f).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_MEDIUM
            }
            setStartVelocity(velocity)
            start()
        }
        
        alphaAnimator = ValueAnimator.ofFloat(panelAlpha, 1f).apply {
            duration = 200
            addUpdateListener {
                val alpha = it.animatedValue as Float
                panelAlpha = alpha
                binding.controlPanel.alpha = alpha
                updateBackgroundBlur(alpha)
            }
            start()
        }
        
        panelTranslationY = 0f
    }
    
    private fun animateExit() {
        // Animate out and finish
        translationSpring = SpringAnimation(binding.controlPanel, SpringAnimation.TRANSLATION_Y, -maxDragHeight).apply {
            spring = SpringForce(-maxDragHeight).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                stiffness = SpringForce.STIFFNESS_HIGH
            }
            start()
        }
        
        alphaAnimator = ValueAnimator.ofFloat(panelAlpha, 0f).apply {
            duration = 200
            addUpdateListener {
                val alpha = it.animatedValue as Float
                binding.controlPanel.alpha = alpha
                updateBackgroundBlur(alpha)
            }
            start()
        }
        
        // Finish activity after animation
        binding.controlPanel.postDelayed({
            finish()
            overridePendingTransition(0, 0)
        }, 300)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        animateExit()
        return true
    }
    
    override fun onBackPressed() {
        animateExit()
    }
}
