package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.FragmentExploreBinding

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

        setupRecycler()
        setupSearch()
    }

    private fun setupRecycler() {

        originalList = listOf(
            CategoryModel("Mess", R.drawable.img_cat_mess),
            CategoryModel("Room", R.drawable.img_cat_room),
            CategoryModel("Tuition", R.drawable.img_cat_tuition),
            CategoryModel("Street Food", R.drawable.img_cat_streetfood),
            CategoryModel("Medical", R.drawable.img_cat_medical),
            CategoryModel("Stationery", R.drawable.img_cat_stationary),
            CategoryModel("Fitness", R.drawable.img_cat_fitness),
            CategoryModel("Others", R.drawable.img_cat_collegeservices)
        )

        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)

        categoryAdapter = CategoryGridAdapter(originalList) { category: CategoryModel ->
            val fragment = Student_ServiceListFragment().apply {
                arguments = Bundle().apply {
                    putString("category", category.name)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvCategories.adapter = categoryAdapter
    }

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