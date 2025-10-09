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
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.components.LiquidButton
import androidx.core.graphics.toColorInt
import com.kyant.backdrop.xml.extensions.frostedGlassOverlay
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import androidx.core.net.toUri
import androidx.core.graphics.drawable.toDrawable
import com.kyant.backdrop.xml.views.LiquidGlassView

class ButtonsActivity : AppCompatActivity() {

    private var selectedWallpaperUri: Uri? = null

    // Modern image picker launcher
//    private val pickImageLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                val imageUri: Uri? = data?.data
//                imageUri?.let {
//                    selectedWallpaperUri = it
//                    applyImageAsWallpaper(it)
//                    recreateActivity()
//                }
//            }
//        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()


        setContentView(R.layout.activity_buttons)


//        // Restore wallpaper if already selected
//        val savedUriString = savedInstanceState?.getString("wallpaper_uri")
//        if (savedUriString != null) {
//            selectedWallpaperUri = savedUriString.toUri()
//
//            applyImageAsWallpaper(selectedWallpaperUri!!)
//        } else {
//            window.decorView.setBackgroundResource(R.drawable.wallpaper)
//        }
//
//        val density = resources.displayMetrics.density
//
//        val mainLayout = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            gravity = Gravity.CENTER
//            setPadding(
//                (24 * density).toInt(),
//                (64 * density).toInt(),
//                (24 * density).toInt(),
//                (64 * density).toInt()
//            )
//        }
//
//        fun buttonLayoutParams(): LinearLayout.LayoutParams {
//            return LinearLayout.LayoutParams(
//                (260 * density).toInt(),
//                (56 * density).toInt()
//            ).apply {
//                gravity = Gravity.CENTER_HORIZONTAL
//                bottomMargin = (20 * density).toInt()
//            }
//        }
//
//        // Transparent Button
//        val button1 = LiquidButton(this).apply {
//            layoutParams = buttonLayoutParams()
//            setText("Transparent Button")
//            setTextColor(Color.BLACK)
//        }
//        mainLayout.addView(button1)
//
//        // Surface Button
//        val button2 = LiquidButton(this).apply {
//            layoutParams = buttonLayoutParams()
//            setText("Surface Button")
//            setTextColor(Color.BLACK)
//            surfaceColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f).toArgb()
//        }
//
//
//        mainLayout.addView(button2)
//
//        // Blue Button
//        val button3 = LiquidButton(this).apply {
//            layoutParams = buttonLayoutParams()
//            setText("Blue Tinted Button")
//            setTextColor(Color.WHITE)
//            tintColor = "#1E88E5".toColorInt()
//        }
//        mainLayout.addView(button3)
//
//// Change Wallpaper Button
//        val pickImageButton = LiquidButton(this).apply {
//            layoutParams = buttonLayoutParams()
//            setText("Change Wallpaper")
//            setTextColor(Color.BLACK)
////            tintColor = "#4CAF50".toColorInt()
//            setOnClickListener {
//                openGallery()
//            }
//        }
//        mainLayout.addView(pickImageButton)
//        // Orange Button
//        val button4 = LiquidButton(this).apply {
//            layoutParams = buttonLayoutParams()
//            setText("Orange Tinted Button")
//            setTextColor(Color.WHITE)
//            tintColor = "#FF9E3D".toColorInt()
//        }
//        mainLayout.addView(button4)
//
//
//
//        setContentView(mainLayout)
//    }
//
//    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        intent.type = "image/*"
//        pickImageLauncher.launch(intent)
//    }
//
//    private fun applyImageAsWallpaper(uri: Uri) {
//        val inputStream = contentResolver.openInputStream(uri)
//        val bitmap = BitmapFactory.decodeStream(inputStream)
//        inputStream?.close()
//        if (bitmap != null) {
//            window.decorView.background = bitmap.toDrawable(resources)
//        }
//    }
//
//    // --- Recreate activity cleanly to redraw UI ---
//    private fun recreateActivity() {
//        // Small delay so wallpaper sets before recreation
//        window.decorView.postDelayed({
//            recreate()
//        }, 200)
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        selectedWallpaperUri?.let {
//            outState.putString("wallpaper_uri", it.toString())
//        }
  }
}
