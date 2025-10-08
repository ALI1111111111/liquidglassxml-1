
package com.kyant.backdrop.catalog.xml.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyant.backdrop.catalog.xml.databinding.ItemCatalogDemoBinding
import com.kyant.backdrop.catalog.xml.databinding.ItemCatalogHeaderBinding
import com.kyant.backdrop.catalog.xml.models.CatalogItem

/**
 * RecyclerView adapter for the catalog list, supporting both header and demo items.
 * Replicates the navigation functionality of the original Compose catalog.
 */
class CatalogAdapter(
    private val items: List<CatalogItem>,
    private val onDemoClick: (CatalogItem.Demo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DEMO = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CatalogItem.Header -> TYPE_HEADER
            is CatalogItem.Demo -> TYPE_DEMO
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemCatalogHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_DEMO -> {
                val binding = ItemCatalogDemoBinding.inflate(inflater, parent, false)
                DemoViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CatalogItem.Header -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is CatalogItem.Demo -> {
                (holder as DemoViewHolder).bind(item, onDemoClick)
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    class HeaderViewHolder(private val binding: ItemCatalogHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: CatalogItem.Header) {
            binding.headerText.text = header.title
        }
    }
    
    class DemoViewHolder(private val binding: ItemCatalogDemoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(demo: CatalogItem.Demo, onDemoClick: (CatalogItem.Demo) -> Unit) {
            binding.titleText.text = demo.title
            binding.descriptionText.text = demo.description
            binding.root.setOnClickListener {
                onDemoClick(demo)
            }
        }
    }
}