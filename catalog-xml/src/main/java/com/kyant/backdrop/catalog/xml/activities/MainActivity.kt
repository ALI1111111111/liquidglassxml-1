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
            CatalogItem.Demo("Liquid Test", "Test liquid glass effects visibility"),
            
            // Liquid glass components section
            CatalogItem.Header("Liquid glass components"),
            CatalogItem.Demo("Buttons", "Liquid glass buttons with different styles"),
            CatalogItem.Demo("Slider", "Liquid glass sliders with different styles"),
            CatalogItem.Demo("Bottom tabs", "Glass bottom tab bar with selection"),
            CatalogItem.Demo("Dialog", "Glass dialog with backdrop effects"),
            
            // System UIs section
            CatalogItem.Header("System UIs"),
            CatalogItem.Demo("Lock screen", "iOS-style lock screen with glass elements"),
            CatalogItem.Demo("Control center", "Glass control center interface"),
            CatalogItem.Demo("Magnifier", "Draggable magnifier with glass effects"),
            
            // Experiments section
            CatalogItem.Header("Experiments"),
            CatalogItem.Demo("Glass playground", "Interactive playground to test effects"),
            CatalogItem.Demo("Adaptive luminance glass", "Glass that adapts to background brightness"),
            CatalogItem.Demo("Scroll container", "Scrollable content with glass backgrounds"),
            CatalogItem.Demo("Lazy scroll container", "RecyclerView with glass items")
        )
    }
    
    private fun navigateToDemo(item: CatalogItem.Demo) {
        val intent = when (item.title) {
            "Liquid Test" -> Intent(this, LiquidTestActivity::class.java)
            "Glass playground" -> Intent(this, GlassPlaygroundActivity::class.java)
            "Buttons" -> Intent(this, ButtonsActivity::class.java)
            "Slider" -> Intent(this, com.kyant.backdrop.catalog.xml.SliderActivity::class.java)
            "Bottom tabs" -> Intent(this, BottomTabsActivity::class.java)
            "Dialog" -> Intent(this, DialogActivity::class.java)
            "Lock screen" -> Intent(this, LockScreenActivity::class.java)
            "Control center" -> Intent(this, ControlCenterActivity::class.java)
            "Magnifier" -> Intent(this, MagnifierActivity::class.java)
            "Adaptive luminance glass" -> Intent(this, AdaptiveLuminanceGlassActivity::class.java)
            "Scroll container" -> Intent(this, ScrollContainerActivity::class.java)
            "Lazy scroll container" -> Intent(this, LazyScrollContainerActivity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}