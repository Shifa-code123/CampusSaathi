package com.example.campussaathi

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.FragmentNearmeBinding

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
        // Header and Drawer logic moved to StudentActivity
        setupCategories()
    }

    private fun setupCategories() {
        binding.categoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRecycler.adapter = NearmeCategoryAdapter(categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}