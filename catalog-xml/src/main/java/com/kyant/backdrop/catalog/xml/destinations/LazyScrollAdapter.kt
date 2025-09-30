package com.kyant.backdrop.catalog.xml.destinations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyant.backdrop.catalog.xml.databinding.ItemGlassBinding

class LazyScrollAdapter : RecyclerView.Adapter<LazyScrollAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGlassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // No data to bind, just display the view
    }

    override fun getItemCount(): Int = 100

    class ViewHolder(binding: ItemGlassBinding) : RecyclerView.ViewHolder(binding.root)
}
