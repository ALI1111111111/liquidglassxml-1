package com.kyant.backdrop.catalog.xml.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.setPadding
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.BlurEffect
import com.kyant.backdrop.xml.effects.ColorFilterEffect
import com.kyant.backdrop.xml.effects.HighlightEffect
import com.kyant.backdrop.xml.effects.RefractionEffect
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import kotlin.math.max
import kotlin.math.min

class ControlCenterActivity : AppCompatActivity() {

    private var selectedWallpaperUri: android.net.Uri? = null

    // Swipe & animation
    private var progress = 0f // 0 = hidden, 1 = fully visible
    private var initialY = 0f
    private val maxDragDistance = 1000f

    private val tileViews = mutableListOf<LiquidGlassContainer>()
    private lateinit var mainLayout: LinearLayout
    private lateinit var backgroundView: View

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedWallpaperUri = uri
                    applyImageAsWallpaper(uri)
                    recreateActivity()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Restore wallpaper
        val savedUriString = savedInstanceState?.getString("wallpaper_uri")
        if (savedUriString != null) {
            selectedWallpaperUri = savedUriString.toUri()
            applyImageAsWallpaper(selectedWallpaperUri!!)
        } else {
            window.decorView.setBackgroundResource(R.drawable.wallpaper_light)
        }

        val density = resources.displayMetrics.density
        val itemSize = (68 * density).toInt()
        val itemSpacing = (16 * density).toInt()
        val itemTwoSpanSize = itemSize * 2 + itemSpacing
        val cornerRadius = itemSize / 2f

        // Root layout
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(itemSpacing, (80 * density).toInt(), itemSpacing, itemSpacing)
        }

        backgroundView = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0f
        }

        // Function to create animated tile with swipe & enter animation
        fun createAnimatedTile(width: Int, height: Int, hasIcons: Boolean = false): LiquidGlassContainer {
            val tile = createGlassTile(width, height, cornerRadius, hasIcons)
            tile.alpha = 0f
            tile.scaleX = 0.8f
            tile.scaleY = 0.8f
            tileViews.add(tile)
            return tile
        }

        // --- Layout: same as Compose ---
        // Row 1
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_HORIZONTAL }
        row1.addView(createAnimatedTile(itemTwoSpanSize, itemTwoSpanSize))
        row1.addView(createSpacer(itemSpacing, itemSpacing))
        row1.addView(createAnimatedTile(itemTwoSpanSize, itemTwoSpanSize))
        mainLayout.addView(row1)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))

        // Row 2
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val leftCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRow.addView(createAnimatedTile(itemSize, itemSize))
        topRow.addView(createSpacer(itemSpacing, itemSpacing))
        topRow.addView(createAnimatedTile(itemSize, itemSize))
        leftCol.addView(topRow)
        leftCol.addView(createSpacer(itemSpacing, itemSpacing))
        leftCol.addView(createAnimatedTile(itemTwoSpanSize, itemSize))
        row2.addView(leftCol)
        row2.addView(createSpacer(itemSpacing, itemSpacing))
        val rightCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        rightCol.addView(createAnimatedTile(itemSize, itemTwoSpanSize))
        rightCol.addView(createSpacer(itemSpacing, itemSpacing))
        rightCol.addView(createAnimatedTile(itemSize, itemTwoSpanSize))
        row2.addView(rightCol)
        mainLayout.addView(row2)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))


        // Row 3
        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(createAnimatedTile(itemTwoSpanSize, itemTwoSpanSize))
        row3.addView(createSpacer(itemSpacing, itemSpacing))
        val rightCol3 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val topRow3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRow3.addView(createAnimatedTile(itemSize, itemSize))
        topRow3.addView(createSpacer(itemSpacing, itemSpacing))
        topRow3.addView(createAnimatedTile(itemSize, itemSize))
        rightCol3.addView(topRow3)
        rightCol3.addView(createSpacer(itemSpacing, itemSpacing))
        rightCol3.addView(createAnimatedTile(itemSize, itemSize))
        row3.addView(rightCol3)
        mainLayout.addView(row3)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))

        // Change Wallpaper Button
        val changeWallpaperBtn = Button(this).apply {
            text = "Change Wallpaper"
            setBackgroundColor(Color.argb(40, 255, 255, 255))
            setTextColor(Color.BLACK)
            setPadding(24, 12, 24, 12)
            setOnClickListener { openGallery() }
        }
        mainLayout.addView(changeWallpaperBtn)

        // Wrap background and layout
        val rootLayout = FrameLayout(this)
        rootLayout.addView(backgroundView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        rootLayout.addView(mainLayout)
        setContentView(rootLayout)

        // Enter animation staggered
        staggerEnterAnimation()

        // Swipe handling
        mainLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> initialY = event.rawY
                MotionEvent.ACTION_MOVE -> {
                    val delta = initialY - event.rawY
                    progress = (1f - delta / maxDragDistance).coerceIn(0f, 1f)
                    updateTilesWithProgress(progress)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    progress = if (progress < 0.5f) 0f else 1f
                    updateTilesWithProgress(progress)
                }
            }
            true
        }
    }


    private fun updateTilesWithProgress(progress: Float) {
        backgroundView.alpha = 0.4f * progress
        tileViews.forEach { tile ->
            tile.alpha = progress
            tile.scaleX = 0.9f + 0.1f * progress
            tile.scaleY = 0.9f + 0.1f * progress

            // Clamp blur to at least 0.1f to avoid IllegalArgumentException
            val blurRadius = max(0.1f, 16f * progress)
            tile.setBlurEffect(BlurEffect(blurRadius))

            tile.setRefractionEffect(RefractionEffect(48f * progress, 96f * progress, true))
        }
    }


    private fun staggerEnterAnimation() {
        val handler = Handler(Looper.getMainLooper())
        tileViews.forEachIndexed { index, tile ->
            handler.postDelayed({
                tile.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()
            }, (index * 80).toLong())
        }
        handler.postDelayed({ backgroundView.animate().alpha(0.4f).setDuration(250).start() }, 0)
        progress = 1f
    }

    private fun createGlassTile(width: Int, height: Int, cornerRadius: Float, hasIcons: Boolean): LiquidGlassContainer {
        val density = resources.displayMetrics.density
        return LiquidGlassContainer(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            setCornerRadius(cornerRadius)
            setBlurEffect(BlurEffect(16 * density))
            setRefractionEffect(RefractionEffect(48 * density, 96 * density, true))
            setHighlightEffect(HighlightEffect.topLeft(falloff = 1.8f))
            setColorFilterEffect(ColorFilterEffect.vibrant())

        }
    }

    private fun createSpacer(width: Int, height: Int): View =
        View(this).apply { layoutParams = LinearLayout.LayoutParams(width, height) }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun applyImageAsWallpaper(uri: android.net.Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap != null) window.decorView.background = bitmap.toDrawable(resources)
    }

    private fun recreateActivity() {
        window.decorView.postDelayed({ recreate() }, 200)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedWallpaperUri?.let { outState.putString("wallpaper_uri", it.toString()) }
    }
}
