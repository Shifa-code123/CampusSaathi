package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminFragmentApprovalsBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragmentApprovals : Fragment() {

    private var _binding: AdminFragmentApprovalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminApprovalAdapter
    private val db = FirebaseFirestore.getInstance()
    private var currentTab = "Owners"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentApprovalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = AdminApprovalAdapter(mutableListOf(), 
            onApprove = { item -> updateStatus(item, "approved") },
            onReject = { item -> updateStatus(item, "rejected") },
            onItemClick = { item ->
                val intent = Intent(requireContext(), AdminApprovalDetailActivity::class.java).apply {
                    putExtra("DOC_ID", item.id)
                    putExtra("TYPE", item.type)
                }
                startActivity(intent)
            }
        )
        binding.rvApprovals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApprovals.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.text.toString()
                fetchData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        val collection = if (currentTab == "Owners") "owner_verifications" else "services"
        
        db.collection(collection)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val list = mutableListOf<AdminApprovalModel>()
                for (doc in documents) {
                    val title = if (currentTab == "Owners") doc.getString("fullName") ?: "Unknown" else doc.getString("serviceName") ?: "Unknown"
                    val subtitle = if (currentTab == "Owners") doc.getString("phone") ?: "" else doc.getString("category") ?: ""
                    list.add(AdminApprovalModel(doc.id, title, subtitle, "pending", if (currentTab == "Owners") "owner" else "service", doc.data))
                }
                adapter.updateData(list)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(item: AdminApprovalModel, status: String) {
        val collection = if (item.type == "owner") "owner_verifications" else "services"
        db.collection(collection).document(item.id)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Updated to $status", Toast.LENGTH_SHORT).show()
                adapter.removeItem(item)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}