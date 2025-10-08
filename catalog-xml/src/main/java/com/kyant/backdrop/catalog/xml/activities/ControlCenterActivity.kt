package com.kyant.backdrop.catalog.xml.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.setPadding
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassContainer

class ControlCenterActivity : AppCompatActivity() {

    private var selectedWallpaperUri: Uri? = null

    // Image picker launcher
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

        // --- Restore wallpaper if exists ---
        val savedUriString = savedInstanceState?.getString("wallpaper_uri")
        if (savedUriString != null) {
            selectedWallpaperUri = savedUriString.toUri()
            applyImageAsWallpaper(selectedWallpaperUri!!)
        } else {
            window.decorView.setBackgroundResource(R.drawable.wallpaper_light)
        }

        // --- Layout constants ---
        val density = resources.displayMetrics.density
        val itemSize = (68 * density).toInt()
        val itemSpacing = (16 * density).toInt()
        val itemTwoSpanSize = itemSize * 2 + itemSpacing
        val cornerRadius = itemSize / 2f

        // --- Main layout ---
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(itemSpacing, (80 * density).toInt(), itemSpacing, itemSpacing)
        }

        // Row 1 (2 large tiles)
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        row1.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, true))
        row1.addView(createSpacer(itemSpacing, itemSpacing))
        row1.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, false))
        mainLayout.addView(row1)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))

        // Row 2 (mixed)
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        topRow.addView(createSpacer(itemSpacing, itemSpacing))
        topRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        leftColumn.addView(topRow)
        leftColumn.addView(createSpacer(itemSpacing, itemSpacing))
        leftColumn.addView(createGlassTile(itemTwoSpanSize, itemSize, cornerRadius, false))

        val rightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        rightRow.addView(createGlassTile(itemSize, itemTwoSpanSize, cornerRadius, false))
        rightRow.addView(createSpacer(itemSpacing, itemSpacing))
        rightRow.addView(createGlassTile(itemSize, itemTwoSpanSize, cornerRadius, false))

        row2.addView(leftColumn)
        row2.addView(createSpacer(itemSpacing, itemSpacing))
        row2.addView(rightRow)

        mainLayout.addView(row2)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))

        // Row 3 (large + smalls)
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row3.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, false))
        row3.addView(createSpacer(itemSpacing, itemSpacing))

        val rightColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val topRightRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRightRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        topRightRow.addView(createSpacer(itemSpacing, itemSpacing))
        topRightRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        rightColumn.addView(topRightRow)
        rightColumn.addView(createSpacer(itemSpacing, itemSpacing))
        rightColumn.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))

        row3.addView(rightColumn)
        mainLayout.addView(row3)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))

        // --- Add wallpaper picker button at the bottom ---
        val changeWallpaperBtn = Button(this).apply {
            text = "Change Wallpaper"
            setBackgroundColor(Color.argb(40, 255, 255, 255))
            setTextColor(Color.BLACK)
            setPadding(24, 12, 24, 12)
            setOnClickListener { openGallery() }
        }
        mainLayout.addView(changeWallpaperBtn)

        setContentView(mainLayout)
    }

    private fun createGlassTile(width: Int, height: Int, cornerRadius: Float, hasIcons: Boolean): LiquidGlassContainer {
        val density = resources.displayMetrics.density

        return LiquidGlassContainer(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            setCornerRadius(cornerRadius)

            // ✅ Use real API calls that exist in your effects package
            setBlurEffect(BlurEffect(16 * density))
            setRefractionEffect(RefractionEffect(48 * density, 96 * density, true))

            // Use the existing helper used earlier in your project
            // (original ControlCenter used HighlightEffect.topLeft(falloff = 2f))
            setHighlightEffect(HighlightEffect.topLeft(falloff = 1.8f))

            // Use an available color filter factory (vibrant() was used earlier)
            setColorFilterEffect(ColorFilterEffect.vibrant())

            // ShadowEffect constructor: offsetX, offsetY, radius, color, alpha
            // Provide reasonable offsets and radius using density
            val shadowOffsetX = 0f
            val shadowOffsetY = 6f * density
            val shadowRadius = 32f * density
            setShadowEffect(ShadowEffect(shadowOffsetX, shadowOffsetY, shadowRadius, Color.BLACK, 0.25f))

            setBackgroundColor(Color.argb(18, 255, 255, 255))

            if (hasIcons) {
                val iconLayout = FrameLayout(context).apply {
                    setPadding((16 * density).toInt())
                }

                val iconSize = (56 * density).toInt()
                val iconCornerRadius = iconSize / 2f

                val inactive = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                        gravity = Gravity.TOP or Gravity.START
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.argb(51, 255, 255, 255))
                }

                val activeBlue = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                        gravity = Gravity.TOP or Gravity.END
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.parseColor("#0088FF"))
                }

                val activeBottom = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                        gravity = Gravity.BOTTOM or Gravity.START
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.parseColor("#0088FF"))
                }

                iconLayout.addView(inactive)
                iconLayout.addView(activeBlue)
                iconLayout.addView(activeBottom)
                addView(iconLayout)
            }
        }
    }

    private fun createSpacer(width: Int, height: Int): View =
        View(this).apply { layoutParams = LinearLayout.LayoutParams(width, height) }

    // --- Wallpaper logic ---
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun applyImageAsWallpaper(uri: Uri) {
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
