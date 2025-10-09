package com.kyant.backdrop.catalog.xml.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
 * - Real-time backdrop updates with image picker
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
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadBackgroundImage(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlCenterDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setupToolbar()
        setupGlassCards()
        setupDragGesture()
        
        // Initial enter animation
        animateEnter()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Control Center"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }
    
    private fun loadBackgroundImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Set the bitmap to LayerBackdropView
            // Blur will be applied by setBackdropBlur(), so glass cards read blurred version
            binding.backdropLayer.setBackgroundImage(bitmap)
            
            Toast.makeText(this, "Background updated! Glass effects update in real-time.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupGlassCards() {
        val backdrop = binding.backdropLayer.getBackdrop()
        val density = resources.displayMetrics.density
        
        // Glass effect matching Compose: vibrancy() + refraction(24dp * progress, 48dp * progress, true)
        // Vibrancy is now DEFAULT in the library (saturation 1.5x)
        // Initial setup with progress = 1.0 (fully visible)
        
        // Item size from Compose: 68dp, corner radius = 34dp (half)
        val cornerRadius = 34f * density
        
        // Surface color from Compose: Color.Black.copy(0.05f) = Black 5% alpha
        val surfaceColor = Color.argb(13, 0, 0, 0)
        
        // Apply effects to all glass cards using new library API
        listOf(
            binding.gridCard,
            binding.displayCard,
            binding.smallCard1,
            binding.smallCard2,
            binding.wideCard,
            binding.tallCard,
            binding.tallCard2
        ).forEach { card ->
            // Set backdrop source
            card.setBackgroundSource(backdrop)
            
            // Corner radius (capsule shape for items)
            card.setCornerRadius(cornerRadius)
            
            // Refraction effect - matching Compose refraction(24dp * progress, 48dp * progress, true)
            // With progress = 1.0 (fully visible)
            card.setRefractionEffect(RefractionEffect(
                height = 24f * density,
                amount = 48f * density,
                hasDepthEffect = true
            ))
            
            // Blur effect - matching Compose architecture
            // Compose has: blur(8dp) on glass + blur(4dp) on background = 12dp total perceived blur
            // With background blur enabled, glass cards only need 8dp
            card.setBlurEffect(BlurEffect(8f * density))
            
            // Highlight effect - matching Compose highlight with 30% alpha for prominence
            card.setHighlightEffect(HighlightEffect.ambient(alpha = 0.3f))
            
            // NO surface tint - let glass be fully transparent to show backdrop
            // Compose version doesn't use onDrawSurface, relies on pure glass effect
            
            // Vibrancy is already applied by DEFAULT in the library!
            // No need to call setColorFilterEffect(ColorFilterEffect.vibrant())
        }
        
        // Setup button clicks
        setupButtonClicks()
    }
    
    private fun setupButtonClicks() {
        // WiFi Button - with touch feedback for backdrop updates
        binding.wifiButton.setOnClickListener {
            Toast.makeText(this, "WiFi toggled", Toast.LENGTH_SHORT).show()
        }
        binding.wifiButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Force backdrop update during button press for real-time glass effects
                    binding.backdropLayer.invalidateLayer()
                }
            }
            false // Let click listener handle the click
        }
        
        // Bluetooth Button - with touch feedback
        binding.bluetoothButton.setOnClickListener {
            Toast.makeText(this, "Bluetooth toggled", Toast.LENGTH_SHORT).show()
        }
        binding.bluetoothButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.backdropLayer.invalidateLayer()
                }
            }
            false
        }
        
        // Airplane Button - with touch feedback
        binding.airplaneButton.setOnClickListener {
            // Open image picker when airplane button is clicked
            openImagePicker()
        }
        binding.airplaneButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.backdropLayer.invalidateLayer()
                }
            }
            false
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
        val density = resources.displayMetrics.density
        
        binding.controlPanel.apply {
            translationY = panelTranslationY
            alpha = panelAlpha
            
            // Scale effect when dragging - matching Compose glassLayer
            val progress = abs(panelTranslationY / maxDragHeight).coerceIn(0f, 1f)
            scaleX = 1f - (0.1f * progress)
            scaleY = 1f + (0.1f * progress)
        }
        
        // Update glass card refraction effects dynamically during animation
        // Matching Compose: refraction(24dp * safeProgress, 48dp * safeProgress, true)
        val safeProgress = panelAlpha // Alpha goes 1.0 (visible) -> 0.0 (hidden)
        
        listOf(
            binding.gridCard,
            binding.displayCard,
            binding.smallCard1,
            binding.smallCard2,
            binding.wideCard,
            binding.tallCard,
            binding.tallCard2
        ).forEach { card ->
            // Animate refraction: full effect when visible, reduced when dragging away
            card.setRefractionEffect(RefractionEffect(
                height = 24f * density * safeProgress,
                amount = 48f * density * safeProgress,
                hasDepthEffect = true
            ))
            
            // Request re-render with updated effects
            card.postInvalidateOnAnimation()
        }
        
        // CRITICAL: Force backdrop layer to update during animation
        // This ensures glass effects capture the moving backdrop in real-time
        binding.backdropLayer.invalidateLayer()
    }
    
    private fun updateBackgroundBlur(alpha: Float) {
        // Apply blur and dim DIRECTLY to LayerBackdropView (matching Compose architecture)
        // Compose: Image.layerBackdrop(backdrop).then(modifier.graphicsLayer { renderEffect = BlurEffect(...) })
        // The blur is applied to the captured layer, so glass cards read BLURRED backdrop
        val blurRadius = 4f * resources.displayMetrics.density * alpha // Compose uses blur(4.dp)
        val dimAlpha = 0.4f * alpha // dimColor = Color.Black.copy(0.4f) in Compose
        
        binding.backdropLayer.setBackdropBlur(blurRadius, dimAlpha)
    }
    
    private fun animateEnter() {
        val density = resources.displayMetrics.density
        
        // Initial state: off-screen and invisible
        panelTranslationY = -48f * density
        panelAlpha = 0f
        binding.controlPanel.apply {
            translationY = panelTranslationY
            alpha = panelAlpha
        }
        
        // Animate translation to visible
        translationSpring = SpringAnimation(binding.controlPanel, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring = SpringForce(0f).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_MEDIUM
            }
            addUpdateListener { _, value, _ ->
                panelTranslationY = value
                // Update backdrop and effects during spring animation
                binding.backdropLayer.invalidateLayer()
            }
            start()
        }
        
        // Animate alpha and effects
        alphaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener {
                val alpha = it.animatedValue as Float
                panelAlpha = alpha
                
                // Update panel state (refraction effects + blur)
                updatePanelState()
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
            addUpdateListener { _, value, _ ->
                panelTranslationY = value
                // Update backdrop during spring animation
                binding.backdropLayer.invalidateLayer()
            }
            start()
        }
        
        // Animate alpha back to 1.0
        alphaAnimator = ValueAnimator.ofFloat(panelAlpha, 1f).apply {
            duration = 200
            addUpdateListener {
                val alpha = it.animatedValue as Float
                panelAlpha = alpha
                
                // Update panel state (refraction effects + blur)
                updatePanelState()
                updateBackgroundBlur(alpha)
            }
            start()
        }
    }
    
    private fun animateExit() {
        // Animate out and finish
        translationSpring = SpringAnimation(binding.controlPanel, SpringAnimation.TRANSLATION_Y, -maxDragHeight).apply {
            spring = SpringForce(-maxDragHeight).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                stiffness = SpringForce.STIFFNESS_HIGH
            }
            addUpdateListener { _, value, _ ->
                panelTranslationY = value
                // Update backdrop during spring animation
                binding.backdropLayer.invalidateLayer()
            }
            start()
        }
        
        // Animate alpha to 0.0 (fade out effects)
        alphaAnimator = ValueAnimator.ofFloat(panelAlpha, 0f).apply {
            duration = 200
            addUpdateListener {
                val alpha = it.animatedValue as Float
                panelAlpha = alpha
                
                // Update panel state (refraction effects fade out + blur fades out)
                updatePanelState()
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
        super.onBackPressed()
        animateExit()
    }
}
