package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.FragmentExploreBinding
import com.example.campussaathi.utils.DrawerManager

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryGridAdapter
    private lateinit var originalList: List<CategoryModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DrawerManager.setupDrawer(
            requireActivity(),
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        // Header
        binding.header.tvHeaderTitle.text = "Explore"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.header.ivProfile.setOnClickListener {
            startActivity(Intent(requireContext(), StudentProfileActivity::class.java))
        }

        setupRecycler()
        setupSearch()
    }

    // 🔥 CATEGORY GRID
    private fun setupRecycler() {

        originalList = listOf(
            CategoryModel("Mess", R.drawable.img_cat_mess),
            CategoryModel("Room", R.drawable.img_cat_room),
            CategoryModel("Tuition", R.drawable.img_cat_tuition),
            CategoryModel("Street Food", R.drawable.img_cat_streetfood),
            CategoryModel("Medical", R.drawable.img_cat_medical),
            CategoryModel("Stationery", R.drawable.img_cat_stationary),
            CategoryModel("Fitness", R.drawable.img_cat_fitness),
            CategoryModel("Other", R.drawable.img_cat_collegeservices)
        )

        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)

        categoryAdapter = CategoryGridAdapter(originalList)
        binding.rvCategories.adapter = categoryAdapter
    }

    // 🔍 SEARCH
    private fun setupSearch() {

        binding.edtSearch.addTextChangedListener { editable ->

            val query = editable.toString().trim().lowercase()

            val filteredList = originalList.filter {
                it.name.lowercase().contains(query)
            }

            categoryAdapter.updateList(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}