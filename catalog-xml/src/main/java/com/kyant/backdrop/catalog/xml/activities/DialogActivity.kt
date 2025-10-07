
package com.kyant.backdrop.catalog.xml.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Dialog activity demonstrating modal dialogs with glass effects.
 * Equivalent to the Dialog destination in the original Compose catalog.
 */
class DialogActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Implement dialog demo
        supportActionBar?.title = "Dialog"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}