package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminActivityNotificationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminNotificationActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityNotificationBinding
    private lateinit var adapter: AdminNotificationAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdminHeader(
            headerBinding = binding.adminNotificationHeader,
            title = "Notifications",
            showBack = true
        )

        setupRecyclerView()
        fetchNotifications()
    }

    private fun setupRecyclerView() {
        adapter = AdminNotificationAdapter(emptyList())
        binding.rvAdminNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvAdminNotifications.adapter = adapter
    }

    private fun fetchNotifications() {
        binding.adminNotificationProgressBar.visibility = View.VISIBLE
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.adminNotificationProgressBar.visibility = View.GONE
                val list = documents.map { doc ->
                    AdminNotificationModel(
                        title = doc.getString("heading") ?: "No Title",
                        description = doc.getString("caption") ?: "No Description",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                adapter.updateData(list)
            }
            .addOnFailureListener { e ->
                binding.adminNotificationProgressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}