package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminFragmentServiceListBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminServiceListFragment : Fragment() {

    private var _binding: AdminFragmentServiceListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminServiceAdapter
    private val db = FirebaseFirestore.getInstance()
    private var category: String? = null

    companion object {
        private const val ARG_CATEGORY = "category"
        fun newInstance(category: String) = AdminServiceListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_CATEGORY, category)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentServiceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        fetchServices()
    }

    private fun setupToolbar() {
        binding.adminServiceListToolbar.title = category ?: "Services"
        binding.adminServiceListToolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminServiceAdapter(emptyList()) { service ->
            val intent = Intent(requireContext(), AdminApprovalDetailActivity::class.java).apply {
                putExtra("DOC_ID", service.id)
                putExtra("TYPE", "service")
            }
            startActivity(intent)
        }
        binding.rvAdminServiceList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminServiceList.adapter = adapter
    }

    private fun fetchServices() {
        if (category == null) return

        // 🔥 CATEGORY MAPPING (MAIN FIX)
        val categoryMap = mapOf(
            "Stationery" to "Stationary Stores",
            "Medical" to "Medical Stores",
            "Fitness" to "Gym",
            "Street Food" to "Street Food",
            "College Services" to "College Services",
            "Mess" to "Mess",
            "Room" to "Room",
            "Tuition" to "Tuition"
        )

        val firebaseCategory = categoryMap[category] ?: category

        Log.d("DEBUG", "UI Category: $category")
        Log.d("DEBUG", "Firebase Category: $firebaseCategory")

        binding.adminServiceListProgressBar.visibility = View.VISIBLE

        db.collection("services")
            .whereEqualTo("category", firebaseCategory)
            // 🔥 optional future filter:
            // .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                binding.adminServiceListProgressBar.visibility = View.GONE

                Log.d("DEBUG", "Documents found: ${documents.size()}")

                val services = documents.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    AdminServiceModel(
                        id = doc.id,
                        serviceName = doc.getString("serviceName") ?: "N/A",
                        category = doc.getString("category") ?: "N/A",
                        contact = doc.getString("contact") ?: "N/A",
                        description = doc.getString("description") ?: "",
                        photos = doc.get("photos") as? List<String> ?: emptyList(),
                        status = doc.getString("status") ?: "pending"
                    )
                }

                if (services.isEmpty()) {
                    binding.tvAdminNoServices.visibility = View.VISIBLE
                } else {
                    binding.tvAdminNoServices.visibility = View.GONE
                    adapter.updateData(services)
                }
            }
            .addOnFailureListener { e ->
                binding.adminServiceListProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Firestore Error", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}