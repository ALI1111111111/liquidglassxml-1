
package com.kyant.backdrop.catalog.xml.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.SliderActivity
import com.kyant.backdrop.catalog.xml.adapters.CatalogAdapter
import com.kyant.backdrop.catalog.xml.databinding.ActivityMainBinding
import com.kyant.backdrop.catalog.xml.models.CatalogItem


class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        val catalogItems = createCatalogItems()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = CatalogAdapter(catalogItems) { item ->
                navigateToDemo(item)
            }
        }
    }
    
    private fun createCatalogItems(): List<CatalogItem> {
        return listOf(
            // Testing section
            CatalogItem.Header("Testing"),
            CatalogItem.Demo("Liquid Test", "Test liquid glass effects visibility with LayerBackdrop"),
            
            // Liquid glass components section
            CatalogItem.Header("Liquid glass components"),
            CatalogItem.Demo("Buttons Demo", "New liquid glass buttons with LayerBackdrop"),
            CatalogItem.Demo("Slider Demo", "New liquid glass sliders with LayerBackdrop"),
            CatalogItem.Demo("Control Center Demo", "iOS-style control center with LayerBackdrop"),
            CatalogItem.Demo("Buttons (Old)", "Original liquid glass buttons"),
            CatalogItem.Demo("Slider (Old)", "Original liquid glass sliders"),
            CatalogItem.Demo("Bottom tabs", "Glass bottom tab bar with selection"),
//            CatalogItem.Demo("Dialog", "Glass dialog with backdrop effects"),
            
            // System UIs section
            CatalogItem.Header("System UIs"),
            CatalogItem.Demo("Lock screen", "iOS-style lock screen with glass elements"),
            CatalogItem.Demo("Control center", "Glass control center interface"),
//            CatalogItem.Demo("Magnifier", "Draggable magnifier with glass effects"),
            
            // Experiments section
            CatalogItem.Header("Experiments"),
            CatalogItem.Demo("Glass playground", "Interactive playground to test effects"),
            CatalogItem.Demo("Adaptive luminance glass", "Glass that adapts to background brightness"),
//            CatalogItem.Demo("Scroll container", "Scrollable content with glass backgrounds"),
            CatalogItem.Demo("Lazy scroll container", "RecyclerView with glass items")
        )
    }
    
    private fun navigateToDemo(item: CatalogItem.Demo) {
        val intent = when (item.title) {
            // New demos with LayerBackdropView
            "Buttons Demo" -> Intent(this, ButtonsDemoActivity::class.java)
            "Slider Demo" -> Intent(this, SliderDemoActivity::class.java)
            "Control Center Demo" -> Intent(this, ControlCenterDemoActivity::class.java)
            
            // Testing
            "Liquid Test" -> Intent(this, LiquidTestActivity::class.java)
            
            // Original demos
            "Buttons (Old)" -> Intent(this, ButtonsActivity::class.java)
            "Slider (Old)" -> Intent(this, com.kyant.backdrop.catalog.xml.SliderActivity::class.java)
            "Glass playground" -> Intent(this, GlassPlaygroundActivity::class.java)
            "Bottom tabs" -> Intent(this, BottomTabsActivity::class.java)
//            "Dialog" -> Intent(this, DialogActivity::class.java)
            "Lock screen" -> Intent(this, LockScreenActivity::class.java)
            "Control center" -> Intent(this, ControlCenterActivity::class.java)
//            "Magnifier" -> Intent(this, MagnifierActivity::class.java)
            "Adaptive luminance glass" -> Intent(this, AdaptiveLuminanceGlassActivity::class.java)
//            "Scroll container" -> Intent(this, ScrollContainerActivity::class.java)
            "Lazy scroll container" -> Intent(this, LazyScrollContainerActivity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}