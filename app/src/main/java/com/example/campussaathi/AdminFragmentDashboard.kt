package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campussaathi.databinding.AdminFragmentDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragmentDashboard : Fragment() {

    private var _binding: AdminFragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStats()
        setupPendingCounts()
        setupActions()
    }

    private fun setupStats() {
        // Students
        db.collection("users").whereEqualTo("role", "student").get().addOnSuccessListener {
            binding.statStudents.tvStatCount.text = it.size().toString()
            binding.statStudents.tvStatTitle.text = "Students"
        }

        // Owners
        db.collection("owner_verifications").get().addOnSuccessListener {
            binding.statOwners.tvStatCount.text = it.size().toString()
            binding.statOwners.tvStatTitle.text = "Owners"
        }

        // Services
        db.collection("services").get().addOnSuccessListener {
            binding.statServices.tvStatCount.text = it.size().toString()
            binding.statServices.tvStatTitle.text = "Services"
        }

        // Volunteers
        db.collection("volunteer_requests").get().addOnSuccessListener {
            binding.statVolunteers.tvStatCount.text = it.size().toString()
            binding.statVolunteers.tvStatTitle.text = "Volunteer"
        }

        // CityHelp
        db.collection("cityhelp").get().addOnSuccessListener {
            binding.statCityHelp.tvStatCount.text = it.size().toString()
            binding.statCityHelp.tvStatTitle.text = "CityHelp"
        }

        // Admins
        binding.statAdmins.tvStatCount.text = "1"
        binding.statAdmins.tvStatTitle.text = "Admins"
    }

    private fun setupPendingCounts() {
        var servicePending = 0
        var ownerPending = 0

        db.collection("services").whereEqualTo("status", "pending").get().addOnSuccessListener {
            servicePending = it.size()
            binding.tvPendingApprovalsCount.text = (servicePending + ownerPending).toString()
        }

        db.collection("owner_verifications").whereEqualTo("status", "pending").get().addOnSuccessListener {
            ownerPending = it.size()
            binding.tvPendingApprovalsCount.text = (servicePending + ownerPending).toString()
        }

        var volPending = 0
        var cityPending = 0

        db.collection("volunteer_requests").whereEqualTo("status", "pending").get().addOnSuccessListener {
            volPending = it.size()
            binding.tvPendingVolunteersCount.text = (volPending + cityPending).toString()
        }

        db.collection("cityhelp").whereEqualTo("status", "pending").get().addOnSuccessListener {
            cityPending = it.size()
            binding.tvPendingVolunteersCount.text = (volPending + cityPending).toString()
        }
    }

    private fun setupActions() {
        binding.btnManageStudents.btnAction.text = "Manage Students"
        binding.btnManageStudents.btnAction.setOnClickListener {
            openManageActivity("users")
        }

        binding.btnManageOwners.btnAction.text = "Manage Owners"
        binding.btnManageOwners.btnAction.setOnClickListener {
            openManageActivity("owners")
        }

        binding.btnManageVolunteers.btnAction.text = "Manage Volunteers"
        binding.btnManageVolunteers.btnAction.setOnClickListener {
            openManageActivity("volunteers")
        }

        binding.btnManageCityHelp.btnAction.text = "Manage CityHelp"
        binding.btnManageCityHelp.btnAction.setOnClickListener {
            openManageActivity("cityhelp")
        }

        binding.btnManageServices.btnAction.text = "Manage Services"
        binding.btnManageServices.btnAction.setOnClickListener {
            val mainActivity = requireActivity() as? AdminActivityMain
            mainActivity?.setCurrentPage(3) // Services is a main tab
        }

        binding.btnManageOwnerBusiness.btnAction.text = "Owner Business"
        binding.btnManageOwnerBusiness.btnAction.setOnClickListener {
            openManageActivity("owners")
        }
    }

    private fun openManageActivity(type: String) {
        val intent = Intent(requireContext(), AdminManageActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
