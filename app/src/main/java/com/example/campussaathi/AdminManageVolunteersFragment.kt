package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminFragmentManageListBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminManageVolunteersFragment : Fragment() {

    private var _binding: AdminFragmentManageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminGenericAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentManageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        fetchVolunteers()
    }

    private fun setupToolbar() {
        binding.adminManageToolbar.title = "Manage Volunteers"
        binding.adminManageToolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminGenericAdapter(emptyList()) { item ->
            val intent = Intent(requireContext(), AdminManageDetailActivity::class.java).apply {
                putExtra("DOC_ID", item.id)
                putExtra("TYPE", "volunteer")
            }
            startActivity(intent)
        }
        binding.rvManageList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManageList.adapter = adapter
    }

    private fun fetchVolunteers() {
        binding.adminManageProgressBar.visibility = View.VISIBLE
        db.collection("volunteer_requests").get()
            .addOnSuccessListener { documents ->
                binding.adminManageProgressBar.visibility = View.GONE
                val list = documents.map { doc ->
                    AdminManageModel(
                        id = doc.id,
                        title = doc.getString("name") ?: "N/A",
                        subtitle = doc.getString("reason") ?: "N/A",
                        type = "volunteer"
                    )
                }
                adapter.updateData(list)
            }
            .addOnFailureListener {
                binding.adminManageProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Fetch failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}