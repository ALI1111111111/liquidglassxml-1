package com.ali.funsol.glass.liquid.tech.catalog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.ali.funsol.glass.liquid.tech.catalog.databinding.ActivityMainBinding
import com.ali.funsol.glass.liquid.tech.liquidglass.LiquidSlider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.background.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)

        binding.liquidSlider.onValueChangedListener = object : LiquidSlider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                Log.d("MainActivity", "Slider value changed: $value")
            }
        }

        binding.pickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
