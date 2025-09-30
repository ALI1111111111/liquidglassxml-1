package com.kyant.backdrop.catalog.xml.destinations

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyant.backdrop.catalog.xml.CatalogDestination
import com.kyant.backdrop.catalog.xml.components.GlassButton

class HomeAdapter(
    private val destinations: List<CatalogDestination>,
    private val onClick: (CatalogDestination) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(val button: GlassButton) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val button = GlassButton(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16 * parent.resources.displayMetrics.density.toInt()
            }
        }
        return ViewHolder(button)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val destination = destinations[position]
        holder.button.text = destination.title
        holder.button.setOnClickListener {
            onClick(destination)
        }
    }

    override fun getItemCount() = destinations.size
}
