package com.ali.funsol.glass.liquid.tech.liquidglassxml

import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ali.funsol.glass.liquid.tech.liquidglassxml.databinding.ActivityMainBinding
import com.kyant.backdrop.xml.BackdropEffect

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(
                this,
                "Some effects require Android 12+, basic effects will be used",
                Toast.LENGTH_LONG
            ).show()
        }

        // Setup glass views with effects
        binding.glassView.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addBlurEffect(15f)
            }
        }

        binding.circleGlassView.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addBlurEffect(20f)
            }
        }

        setupSeekBars()
    }

    private fun setupSeekBars() {
        // Note: SeekBars control UI only, effects are applied programmatically
        // In the new API, effects need to be reapplied when changed
        binding.blurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    binding.glassView.clearBackdropEffects()
                    binding.glassView.addBlurEffect(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Other seekbars can be used for different effects
        binding.saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Saturation control could adjust color effects
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Brightness control could adjust gamma
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.refractionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Refraction is available on API 33+
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}