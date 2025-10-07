
package com.kyant.backdrop.catalog.xml.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Scroll Container activity demonstrating scrollable content with glass backgrounds.
 * Equivalent to the Scroll Container destination in the original Compose catalog.
 */
class ScrollContainerActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Implement scroll container demo
        supportActionBar?.title = "Scroll Container"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}