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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
//        animateEnter()
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

        // Glass effect matching ControlCenterActivity design
        // Item size from Compose: 68dp, corner radius = 34dp (half)
        val cornerRadius = 34f * density

        // Compose colors (matching ControlCenterActivity):
        // - containerColor = Color.Black.copy(0.05f) = Black 5% alpha
        // - White stroke with 20% alpha for subtle border
        val containerColor = Color.argb(13, 0, 0, 0) // 5% black
        val strokeColor = Color.argb(51, 255, 255, 255) // 20% white
        val strokeWidth = (2f * density).toInt() // 2dp stroke

        // Apply effects to all glass cards using ControlCenterActivity styling
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

            // Refraction effect - matching Compose refraction(24dp, 48dp, true)
            card.setRefractionEffect(RefractionEffect(
                height = 24f * density,
                amount = 48f * density,
                hasDepthEffect = true
            ))


            // Highlight effect - matching ControlCenterActivity (topLeft with 30% alpha)
            card.setHighlightEffect(HighlightEffect.topLeft( falloff = 2f))
            card.setColorFilterEffect(ColorFilterEffect.highContrast())
            // Vibrancy is already applied by DEFAULT in the library
            // NO additional blur on glass tiles - Compose only has blur on backdrop layer

            // Apply background with stroke and surface tint (matching ControlCenterActivity)
            card.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setCornerRadius(cornerRadius)
                setStroke(strokeWidth, strokeColor) // Subtle white stroke (2dp, 20% alpha)
                setColor(containerColor) // Black 5% surface tint
            }
        }

        // Setup button clicks
        setupButtonClicks()
    }

    private fun setupButtonClicks() {
        // WiFi Button - with touch feedback for backdrop updates
        binding.wifiButton.setOnClickListener {
            Toast.makeText(this, "WiFi toggled", Toast.LENGTH_SHORT).show()
        }
        // REMOVED: Excessive touch listener that called invalidateLayer() on every touch event
        // The backdrop updates automatically via pre-draw listener when content changes

        // Bluetooth Button - with touch feedback
        binding.bluetoothButton.setOnClickListener {
            Toast.makeText(this, "Bluetooth toggled", Toast.LENGTH_SHORT).show()
        }
        // REMOVED: Excessive touch listener

        // Airplane Button - opens image picker
        binding.airplaneButton.setOnClickListener {
            openImagePicker()
        }
        // REMOVED: Excessive touch listener
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
//                updateBackgroundBlur(panelAlpha)

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
        // Offload calculations to a background coroutine and apply UI updates on the main thread.
        // Note: arbitrary Kotlin work cannot be forced onto the GPU. Heavy calculations run on a background thread here.
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            val density = resources.displayMetrics.density
            val panelTranslation = panelTranslationY
            val panelAlphaLocal = panelAlpha

            // Compute transforms off the UI thread
            val progress = kotlin.math.abs(panelTranslation / maxDragHeight).coerceIn(0f, 1f)
            val scaleXVal = 1f - (0.1f * progress)
            val scaleYVal = 1f + (0.1f * progress)

            // Refraction params computed off-thread
            val safeProgress = panelAlphaLocal
            val refractionHeight = 24f * density * safeProgress
            val refractionAmount = 48f * density * safeProgress

            // Apply UI changes on the main thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                binding.controlPanel.apply {
                    translationY = panelTranslation
                    alpha = panelAlphaLocal
                    scaleX = scaleXVal
                    scaleY = scaleYVal
                }

                val cards = listOf(
                    binding.gridCard,
                    binding.displayCard,
                    binding.smallCard1,
                    binding.smallCard2,
                    binding.wideCard,
                    binding.tallCard,
                    binding.tallCard2
                )

                cards.forEach { card ->
                    card.setRefractionEffect(RefractionEffect(
                        height = refractionHeight,
                        amount = refractionAmount,
                        hasDepthEffect = true
                    ))
                    card.setColorFilterEffect(ColorFilterEffect.highContrast())

                    card.postInvalidateOnAnimation()
                }

                // Update backdrop on main thread
                binding.backdropLayer.invalidateLayer()
            }
        }
    }

    private fun updateBackgroundBlur(alpha: Float) {
        // Apply blur and dim DIRECTLY to LayerBackdropView (matching Compose architecture)
        // Compose: Image.layerBackdrop(backdrop).then(modifier.graphicsLayer { renderEffect = BlurEffect(4.dp) })
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
                // Update backdrop in real-time during spring animation
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
