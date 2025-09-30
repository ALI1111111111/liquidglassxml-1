package com.kyant.backdrop.catalog.xml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kyant.backdrop.catalog.xml.databinding.ActivityMainBinding
import com.kyant.backdrop.catalog.xml.destinations.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backgroundImage.setImageResource(R.drawable.wallpaper_light)

        if (savedInstanceState == null) {
            navigateTo(HomeFragment())
        }
    }

    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
