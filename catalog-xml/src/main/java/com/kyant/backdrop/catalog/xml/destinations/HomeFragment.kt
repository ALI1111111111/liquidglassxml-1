package com.kyant.backdrop.catalog.xml.destinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyant.backdrop.catalog.xml.CatalogDestination
import com.kyant.backdrop.catalog.xml.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val destinations = CatalogDestination.values().filter { it != CatalogDestination.Home }
        val adapter = HomeAdapter(destinations) { destination ->
            val fragment = when (destination) {
                CatalogDestination.Buttons -> ButtonsFragment()
                CatalogDestination.Slider -> SliderFragment()
                CatalogDestination.BottomTabs -> BottomTabsFragment()
                CatalogDestination.Dialog -> DialogFragment()
                CatalogDestination.LockScreen -> LockScreenFragment()
                CatalogDestination.ControlCenter -> ControlCenterFragment()
                CatalogDestination.Magnifier -> MagnifierFragment()
                CatalogDestination.GlassPlayground -> GlassPlaygroundFragment()
                CatalogDestination.AdaptiveLuminanceGlass -> AdaptiveLuminanceGlassFragment()
                CatalogDestination.ScrollContainer -> ScrollContainerFragment()
                CatalogDestination.LazyScrollContainer -> LazyScrollContainerFragment()
                else -> Fragment() // Placeholder for Home
            }
            (activity as? com.kyant.backdrop.catalog.xml.MainActivity)?.navigateTo(fragment)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
