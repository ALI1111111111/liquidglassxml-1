package com.kyant.backdrop.catalog.xml.models

/**
 * Sealed class representing items in the catalog list.
 * Equivalent to the destinations in the original Compose catalog.
 */
sealed class CatalogItem {
    
    /**
     * Section header item
     */
    data class Header(val title: String) : CatalogItem()
    
    /**
     * Demo item that can be clicked to navigate to a demo screen
     */
    data class Demo(
        val title: String,
        val description: String
    ) : CatalogItem()
}