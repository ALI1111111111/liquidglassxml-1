package com.ali.funsol.glass.liquid.tech.liquidglassxml

import android.graphics.Color
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ali.funsol.glass.liquid.tech.liquidglass.GlassView
import com.ali.funsol.glass.liquid.tech.liquidglassxml.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

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
                saturation = 1.2f
                brightness = 1.1f
            }
        }

        setupSeekBars()
        setupCircleView()
    }

    private fun setupSeekBars() {
        binding.blurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.glassView.blurRadius = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.glassView.saturation = progress.toFloat() / 100f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.glassView.brightness = progress.toFloat() / 100f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.refractionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.glassView.refractionIntensity = progress.toFloat() / 1000f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCircleView() {
        binding.circleGlassView.post {
            val path = Path().apply {
                addCircle(
                    binding.circleGlassView.width / 2f,
                    binding.circleGlassView.height / 2f,
                    binding.circleGlassView.width / 2f,
                    Path.Direction.CW
                )
            }
            binding.circleGlassView.setClipPath(path)
        }
    }
}