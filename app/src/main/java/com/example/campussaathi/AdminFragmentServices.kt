package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.AdminFragmentServicesBinding

class AdminFragmentServices : Fragment() {

    private var _binding: AdminFragmentServicesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf(
            AdminCategoryAdapter.Category("Mess", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Room", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Tuition", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Street Food", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Medical", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Stationery", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Fitness", android.R.drawable.ic_menu_gallery),
            AdminCategoryAdapter.Category("Other", android.R.drawable.ic_menu_gallery)
        )

        val adapter = AdminCategoryAdapter(categories) { categoryName ->
            openServiceList(categoryName)
        }

        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategories.adapter = adapter
    }

    private fun openServiceList(category: String) {
        val fragment = AdminServiceListFragment.newInstance(category)
        // Using childFragmentManager to load the list fragment inside the local container
        // This avoids messing with the parent ViewPager2
        childFragmentManager.beginTransaction()
            .replace(R.id.adminServiceContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}