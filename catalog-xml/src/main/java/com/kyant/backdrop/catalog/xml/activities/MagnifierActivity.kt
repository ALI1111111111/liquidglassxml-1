
package com.kyant.backdrop.catalog.xml.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Magnifier activity demonstrating interactive magnification tool.
 * Equivalent to the Magnifier destination in the original Compose catalog.
 */
class MagnifierActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Implement magnifier demo
        supportActionBar?.title = "Magnifier"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}