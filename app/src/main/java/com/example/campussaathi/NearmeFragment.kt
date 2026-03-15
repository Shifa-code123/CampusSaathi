package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.FragmentNearmeBinding
import com.example.campussaathi.utils.DrawerManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log

class NearmeFragment : Fragment() {

    private var _binding: FragmentNearmeBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        "Hospitals",
        "Police",
        "Medical",
        "Fire Station",
        "Bus Station",
        "Railway Station"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNearmeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // Drawer setup (same as HelpFragment)
        DrawerManager.setupDrawer(
            requireActivity(),
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        // Header title
        binding.header.tvHeaderTitle.text = "Near Me"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        setupCategories()
    }

    // ---------------- CATEGORY GRID ----------------

    private fun setupCategories() {

        binding.categoryRecycler.layoutManager =


            LinearLayoutManager(requireContext())

        binding.categoryRecycler.adapter =
            NearmeCategoryAdapter(categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}