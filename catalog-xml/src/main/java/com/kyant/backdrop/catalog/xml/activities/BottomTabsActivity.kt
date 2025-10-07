
package com.kyant.backdrop.catalog.xml.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassContainer

class BottomTabsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set wallpaper background
        window.decorView.setBackgroundResource(R.drawable.wallpaper_light)
        
        // Create main layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (24 * resources.displayMetrics.density).toInt(),
                (48 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt()
            )
        }
        
        // Add title
        val titleText = TextView(this).apply {
            text = "Bottom Tabs"
            textSize = 28f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, (32 * resources.displayMetrics.density).toInt())
        }
        mainLayout.addView(titleText)
        
        // Create tab examples
        createTabExample(mainLayout, 3)
        createTabExample(mainLayout, 4)
        createTabExample(mainLayout, 5)
        
        setContentView(mainLayout)
    }
    
    private fun createTabExample(parent: LinearLayout, tabCount: Int) {
        val density = resources.displayMetrics.density
        
        // Container for this example
        val exampleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, (24 * density).toInt())
        }
        
        // Label
        val label = TextView(this).apply {
            text = "$tabCount Tabs Example"
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.8f
            setPadding(0, 0, 0, (12 * density).toInt())
        }
        exampleContainer.addView(label)
        
        // Create tab bar
        val tabBar = createTabBar(tabCount)
        exampleContainer.addView(tabBar)
        
        parent.addView(exampleContainer)
    }
    
    private fun createTabBar(tabCount: Int): View {
        val density = resources.displayMetrics.density
        
        // Glass container for tab bar
        val tabBarContainer = LiquidGlassContainer(this).apply {
            setCornerRadius(16 * density)
            setBlurEffect(BlurEffect(8f * density))
            setRefractionEffect(RefractionEffect.subtle(12f * density))
            setColorFilterEffect(ColorFilterEffect.vibrant())
            setHighlightEffect(HighlightEffect.topLeft())
        }
        
        // Tab buttons container
        val tabsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = tabCount.toFloat()
            setPadding(
                (8 * density).toInt(),
                (8 * density).toInt(),
                (8 * density).toInt(),
                (8 * density).toInt()
            )
        }
        
        var selectedTab = 0
        val tabs = mutableListOf<TabButton>()
        
        // Create tab buttons
        for (i in 0 until tabCount) {
            val tab = TabButton(this, "Tab ${i + 1}").apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    (56 * density).toInt(),
                    1f
                )
                tabSelected = i == 0
                
                setOnClickListener {
                    if (selectedTab != i) {
                        tabs[selectedTab].tabSelected = false
                        selectedTab = i
                        this.tabSelected = true
                    }
                }
            }
            tabs.add(tab)
            tabsLayout.addView(tab)
        }
        
        tabBarContainer.addView(tabsLayout)
        
        return tabBarContainer.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }
    
    // Simple tab button implementation
    private class TabButton(context: Context, label: String) : FrameLayout(context) {
        
        private val icon: ImageView
        private val text: TextView
        private val selectedOverlay: LiquidGlassContainer
        
        var tabSelected: Boolean = false
            set(value) {
                field = value
                selectedOverlay.visibility = if (value) View.VISIBLE else View.GONE
                icon.alpha = if (value) 1f else 0.6f
                text.alpha = if (value) 1f else 0.6f
            }
        
        init {
            val density = resources.displayMetrics.density
            
            // Selected state overlay
            selectedOverlay = LiquidGlassContainer(context).apply {
                setCornerRadius(12 * density)
                setBackgroundColor(Color.argb(40, 0, 136, 255))
                setBlurEffect(BlurEffect(4f * density))
                visibility = View.GONE
            }
            addView(selectedOverlay, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            ))
            
            // Content container
            val contentLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            
            // Icon (placeholder)
            icon = ImageView(context).apply {
                setImageResource(android.R.drawable.star_on)
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    (28 * density).toInt(),
                    (28 * density).toInt()
                )
            }
            contentLayout.addView(icon)
            
            // Text
            text = TextView(context).apply {
                text = label
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, (4 * density).toInt(), 0, 0)
            }
            contentLayout.addView(text)
            
            addView(contentLayout, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            ))
            
            // Set clickable
            isClickable = true
            isFocusable = true
        }
    }
}
