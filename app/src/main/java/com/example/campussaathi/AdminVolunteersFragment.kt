package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminFragmentVolunteersBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class AdminVolunteersFragment : Fragment() {

    private var _binding: AdminFragmentVolunteersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminVolunteerAdapter
    private val db = FirebaseFirestore.getInstance()
    private var currentTab = "Volunteer Requests"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentVolunteersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = AdminVolunteerAdapter(mutableListOf(),
            onApprove = { item -> updateStatus(item, "approved") },
            onReject = { item -> updateStatus(item, "rejected") },
            onItemClick = { item ->
                val intent = Intent(requireContext(), AdminVolunteerDetailActivity::class.java).apply {
                    putExtra("DOC_ID", item.id)
                    putExtra("TYPE", item.type)
                }
                startActivity(intent)
            }
        )
        binding.adminRvVolunteers.layoutManager = LinearLayoutManager(requireContext())
        binding.adminRvVolunteers.adapter = adapter
    }

    private fun setupTabs() {
        binding.adminVolunteerTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.text.toString()
                fetchData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchData() {
        binding.adminVolunteerProgressBar.visibility = View.VISIBLE
        val collection = if (currentTab == "Volunteer Requests") "volunteer_requests" else "cityhelp"

        db.collection(collection)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                binding.adminVolunteerProgressBar.visibility = View.GONE
                val list = mutableListOf<AdminVolunteerModel>()
                for (doc in documents) {
                    val title = if (currentTab == "Volunteer Requests") {
                        doc.getString("name") ?: "Unknown"
                    } else {
                        doc.getString("serviceName") ?: doc.getString("category") ?: "Unknown"
                    }
                    val subtitle = if (currentTab == "Volunteer Requests") {
                        doc.getString("phone") ?: ""
                    } else {
                        doc.getString("contact") ?: ""
                    }
                    list.add(AdminVolunteerModel(
                        id = doc.id,
                        title = title,
                        subtitle = subtitle,
                        type = if (currentTab == "Volunteer Requests") "volunteer" else "cityhelp",
                        status = "pending",
                        data = doc.data
                    ))
                }
                adapter.updateData(list)
            }
            .addOnFailureListener { e ->
                binding.adminVolunteerProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(item: AdminVolunteerModel, status: String) {
        val collection = if (item.type == "volunteer") "volunteer_requests" else "cityhelp"
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