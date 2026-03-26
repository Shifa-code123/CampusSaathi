package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.FragmentStudentServiceListBinding
import com.google.firebase.firestore.FirebaseFirestore

class Student_ServiceListFragment : Fragment() {

    private var _binding: FragmentStudentServiceListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: Student_ServiceAdapter

    private val categoryMap = mapOf(
        "Stationery" to "Stationary Store",
        "Medical" to "Medical Store",
        "Fitness" to "Gym",
        "Street Food" to "Street Food",
        "Others" to "Others",
        "Mess" to "Mess",
        "Room" to "Room/Pg",
        "Tuition" to "Tuition"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentServiceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = arguments?.getString("category") ?: ""
        val firestoreCategory = categoryMap[category] ?: category

        (activity as? StudentActivity)?.updateHeaderForFragment(category, isBack = true)

        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())
        adapter = Student_ServiceAdapter(emptyList()) { service ->
            val fragment = Student_ServiceDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("serviceId", service.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvServices.adapter = adapter

        fetchServices(firestoreCategory)
    }

    private fun fetchServices(category: String) {
        FirebaseFirestore.getInstance().collection("services")
            .whereEqualTo("category", category)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Student_ServiceModel::class.java)
                adapter.updateList(list)
                
                if (list.isEmpty()) {
                    // Optional: show a message if no approved services found for this category
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}