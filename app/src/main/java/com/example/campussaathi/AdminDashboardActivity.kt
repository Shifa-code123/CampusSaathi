package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private val ownerList = mutableListOf<AdminVerificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rvOwners)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchAllOwners()
    }

    private fun fetchAllOwners() {

        db.collection("owner_verifications")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->

                ownerList.clear()

                for (doc in snapshot.documents) {
                    ownerList.add(
                        AdminVerificationModel(
                            uid = doc.id,
                            fullName = doc.getString("fullName") ?: "",
                            ownerType = doc.getString("ownerType") ?: ""
                        )
                    )
                }

                recyclerView.adapter = AdminVerificationAdapter(
                    ownerList,
                    onApprove = { approveOwner(it.uid) },
                    onReject = { rejectOwner(it.uid) }
                )
            }
    }

    private fun approveOwner(uid: String) {

        db.collection("owner_verifications").document(uid)
            .update("status", "approved")

        db.collection("users").document(uid)
            .update(
                mapOf(
                    "isVerified" to true,
                    "verificationSubmitted" to true
                )
            )

        Toast.makeText(this, "Owner Approved", Toast.LENGTH_SHORT).show()
        fetchAllOwners() // 🔥 refresh list
    }

    private fun rejectOwner(uid: String) {

        db.collection("owner_verifications").document(uid)
            .update("status", "rejected")

        db.collection("users").document(uid)
            .update("isVerified", false)

        Toast.makeText(this, "Owner Rejected", Toast.LENGTH_SHORT).show()
        fetchAllOwners()
    }
}
