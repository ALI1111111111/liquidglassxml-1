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

        binding.liquidGlassView.setCornerRadius(24f * resources.displayMetrics.density)
        binding.liquidGlassView.setRefraction(
            height = 16f * resources.displayMetrics.density,
            amount = 24f * resources.displayMetrics.density,
            depthEffect = 0.1f
        )

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
