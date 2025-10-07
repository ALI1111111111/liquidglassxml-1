
package com.kyant.backdrop.catalog.xml.activities

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.views.LiquidGlassContainer

class LazyScrollContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set wallpaper background
        window.decorView.setBackgroundResource(R.drawable.ic_launcher_background)
        
        // Create RecyclerView
        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@LazyScrollContainerActivity)
            adapter = GlassItemAdapter()
            
            val density = resources.displayMetrics.density
            setPadding(
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt()
            )
            clipToPadding = false
        }
        
        setContentView(recyclerView)
    }
    
    private class GlassItemAdapter : RecyclerView.Adapter<GlassItemViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlassItemViewHolder {
            return GlassItemViewHolder(parent)
        }
        
        override fun onBindViewHolder(holder: GlassItemViewHolder, position: Int) {
            holder.bind(position)
        }
        
        override fun getItemCount(): Int = 100
    }
    
    private class GlassItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        createItemView(parent)
    ) {
        private val glassContainer = itemView as LiquidGlassContainer
        
        fun bind(position: Int) {
            // Different glass effects for variety
            val density = itemView.resources.displayMetrics.density
            
            when (position % 3) {
                0 -> {
                    // Vibrant glass
                    glassContainer.setRefractionEffect(RefractionEffect.subtle(8 * density))
                    glassContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
                }
                1 -> {
                    // Strong refraction
                    glassContainer.setRefractionEffect(RefractionEffect.strong(16 * density))
                    glassContainer.setColorFilterEffect(ColorFilterEffect.bright(0.2f))
                }
                2 -> {
                    // With dispersion
                    glassContainer.setRefractionEffect(RefractionEffect.subtle(10 * density))
                    glassContainer.setDispersionEffect(DispersionEffect.subtle(6 * density))
                    glassContainer.setColorFilterEffect(ColorFilterEffect(saturation = 1.2f))
                }
            }
        }
        
        companion object {
            fun createItemView(parent: ViewGroup): LiquidGlassContainer {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                
                return LiquidGlassContainer(context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        (160 * density).toInt()
                    ).apply {
                        bottomMargin = (16 * density).toInt()
                    }
                    
                    setCornerRadius(32 * density)
                    setBlurEffect(BlurEffect(10 * density))
                    setHighlightEffect(HighlightEffect.topLeft(falloff = 2f))
                    
                    // Add some padding
                    setPadding(
                        (24 * density).toInt(),
                        (24 * density).toInt(),
                        (24 * density).toInt(),
                        (24 * density).toInt()
                    )
                }
            }
        }
    }
}
