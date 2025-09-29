package com.ali.funsol.glass.liquid.tech.liquidglassxml


import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ali.funsol.glass.liquid.tech.liquidglassxml.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(
                this,
                "Blur requires Android 12+ (fallback not added yet)",
                Toast.LENGTH_LONG
            ).show()
        } else {

            binding.glassView.apply {
                blurRadius = 35f
                cornerRadius = 32f
                tintColor = 0x40FFFFFF // translucent white
                saturation = 1.2f
                brightness = 1.1f
            }
        }
    }
}