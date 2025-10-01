package com.kyant.backdrop.catalog.xml.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.databinding.FragmentGlassDialogBinding

class GlassDialogFragment : DialogFragment() {

    private var _binding: FragmentGlassDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlassDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val density = resources.displayMetrics.density
        
        binding.liquidGlassView.apply {
            setCornerRadius(24f * density)
            setRefraction(
                height = 16f * density,
                amount = 24f * density,
                depthEffect = 0.1f
            )
            
            // Add shadow for depth
            setShadow(com.kyant.backdrop.xml.DefaultShadow(
                elevation = 8f * density,
                color = 0x1A000000.toInt(), // 10% black
                offsetX = 0f,
                offsetY = 4f * density
            ))
            
            // Add inner shadow for depth
            setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
                elevation = 12f * density,
                color = 0x1A000000.toInt(), // 10% black
                offsetX = 0f,
                offsetY = 0f
            ))
            
            setHighlight(2.5f, 1.5f, com.kyant.backdrop.xml.HighlightType.SPECULAR)
            
            // ALWAYS draw frosted surface for dialog (balance between glass effect and readability)
            onDrawSurface = { canvas ->
                // Dialog needs more opacity than buttons, but still frosted glass appearance
                // 40-50% creates readable frosted glass effect
                val surfacePaint = android.graphics.Paint().apply {
                    color = 0xFFFAFAFA.toInt() // Light gray (better than pure white)
                    alpha = 128 // 50% opacity (readable while maintaining frosted glass look)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), surfacePaint)
            }
        }

        binding.okButton.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
