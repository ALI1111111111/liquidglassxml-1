package com.kyant.backdrop.catalog.xml.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassContainer

/**
 * Control Center activity - matches Compose ControlCenterContent
 * Displays iOS-style control center with glass tiles arranged in a grid
 */
class ControlCenterActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        
        // Set wallpaper background
        window.decorView.setBackgroundResource(R.drawable.wallpaper_light)
        
        val density = resources.displayMetrics.density
        val itemSize = (68 * density).toInt()
        val itemSpacing = (16 * density).toInt()
        val itemTwoSpanSize = itemSize * 2 + itemSpacing
        val cornerRadius = itemSize / 2f
        
        // Main container
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                itemSpacing,
                (80 * density).toInt(),
                itemSpacing,
                itemSpacing
            )
        }
        
        // Row 1: Two large 2x2 tiles
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // Connectivity tile (2x2)
        row1.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, true))
        row1.addView(createSpacer(itemSpacing, itemSpacing))
        // Music tile (2x2)
        row1.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, false))
        
        mainLayout.addView(row1)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))
        
        // Row 2: Mixed sizes
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // Left column: 2 small + 1 wide
        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        topRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        topRow.addView(createSpacer(itemSpacing, itemSpacing))
        topRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        leftColumn.addView(topRow)
        
        leftColumn.addView(createSpacer(itemSpacing, itemSpacing))
        leftColumn.addView(createGlassTile(itemTwoSpanSize, itemSize, cornerRadius, false))
        
        row2.addView(leftColumn)
        row2.addView(createSpacer(itemSpacing, itemSpacing))
        
        // Right side: 2 vertical tiles
        val rightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        rightRow.addView(createGlassTile(itemSize, itemTwoSpanSize, cornerRadius, false))
        rightRow.addView(createSpacer(itemSpacing, itemSpacing))
        rightRow.addView(createGlassTile(itemSize, itemTwoSpanSize, cornerRadius, false))
        
        row2.addView(rightRow)
        
        mainLayout.addView(row2)
        mainLayout.addView(createSpacer(itemSpacing, itemSpacing))
        
        // Row 3: Large tile + 2 columns of small tiles
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        row3.addView(createGlassTile(itemTwoSpanSize, itemTwoSpanSize, cornerRadius, false))
        row3.addView(createSpacer(itemSpacing, itemSpacing))
        
        val rightColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val topRightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        topRightRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        topRightRow.addView(createSpacer(itemSpacing, itemSpacing))
        topRightRow.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        rightColumn.addView(topRightRow)
        
        rightColumn.addView(createSpacer(itemSpacing, itemSpacing))
        rightColumn.addView(createGlassTile(itemSize, itemSize, cornerRadius, false))
        
        row3.addView(rightColumn)
        
        mainLayout.addView(row3)
        
        setContentView(mainLayout)
    }
    
    private fun createGlassTile(width: Int, height: Int, cornerRadius: Float, hasIcons: Boolean): LiquidGlassContainer {
        val density = resources.displayMetrics.density
        
        return LiquidGlassContainer(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            setCornerRadius(cornerRadius)
            setBlurEffect(BlurEffect(8 * density))
            setRefractionEffect(RefractionEffect(24 * density, 48 * density, true))
            setColorFilterEffect(ColorFilterEffect.vibrant())
            setHighlightEffect(HighlightEffect.topLeft(falloff = 2f))
            setBackgroundColor(Color.argb(13, 0, 0, 0)) // Black with 5% opacity
            
            if (hasIcons) {
                // Add icon placeholders for connectivity tile
                val iconLayout = FrameLayout(context).apply {
                    setPadding(
                        (16 * density).toInt(),
                        (16 * density).toInt(),
                        (16 * density).toInt(),
                        (16 * density).toInt()
                    )
                }
                
                val innerItemSize = (56 * density).toInt()
                val iconCornerRadius = innerItemSize / 2f
                
                // Top-left icon (inactive)
                val icon1 = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(innerItemSize, innerItemSize).apply {
                        gravity = Gravity.TOP or Gravity.START
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.argb(51, 255, 255, 255)) // White 20%
                }
                iconLayout.addView(icon1)
                
                // Top-right icon (active - blue)
                val icon2 = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(innerItemSize, innerItemSize).apply {
                        gravity = Gravity.TOP or Gravity.END
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.parseColor("#0088FF"))
                }
                iconLayout.addView(icon2)
                
                // Bottom-left icon (active - blue)
                val icon3 = LiquidGlassContainer(context).apply {
                    layoutParams = FrameLayout.LayoutParams(innerItemSize, innerItemSize).apply {
                        gravity = Gravity.BOTTOM or Gravity.START
                    }
                    setCornerRadius(iconCornerRadius)
                    setBackgroundColor(Color.parseColor("#0088FF"))
                }
                iconLayout.addView(icon3)
                
                addView(iconLayout)
            }
        }
    }
    
    private fun createSpacer(width: Int, height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
        }
    }
}