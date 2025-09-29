package com.ali.funsol.glass.liquid.tech.catalog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ali.funsol.glass.liquid.tech.catalog.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.buttonsDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_buttonsFragment)
        }
        binding.sliderDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sliderFragment)
        }
        binding.bottomTabsDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bottomTabsFragment)
        }
        binding.dialogDemoButton.setOnClickListener {
            GlassDialogFragment().show(childFragmentManager, "GlassDialog")
        }
        binding.lockScreenDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_lockScreenFragment)
        }
        binding.controlCenterDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_controlCenterFragment)
        }
        binding.magnifierDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_magnifierFragment)
        }
        binding.glassPlaygroundDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_glassPlaygroundFragment)
        }
        binding.scrollContainerDemoButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scrollContainerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
