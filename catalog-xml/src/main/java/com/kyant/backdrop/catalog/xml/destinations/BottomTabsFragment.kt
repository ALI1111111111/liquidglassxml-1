package com.kyant.backdrop.catalog.xml.destinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.catalog.xml.databinding.FragmentBottomTabsBinding

class BottomTabsFragment : Fragment() {

    private var _binding: FragmentBottomTabsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.glassBottomTabs.setMenu(R.menu.bottom_navigation_menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
