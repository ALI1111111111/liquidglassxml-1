package com.kyant.backdrop.catalog.xml.destinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.databinding.FragmentMagnifierBinding

class MagnifierFragment : Fragment() {

    private var _binding: FragmentMagnifierBinding? = null
    private val binding get() = _binding!!

    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMagnifierBinding.inflate(inflater, container, false)
        setupTouchListener()
        // Load the full lorem ipsum text
        binding.loremIpsumText.text = getString(R.string.lorem_ipsum)
        return binding.root
    }

    private fun setupTouchListener() {
        binding.root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = binding.magnifierView.x
                    initialY = binding.magnifierView.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    binding.magnifierView.x = initialX + deltaX
                    binding.magnifierView.y = initialY + deltaY
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
