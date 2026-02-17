package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private val ownerList = mutableListOf<AdminVerificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.adminToolbar)
        setSupportActionBar(toolbar)

        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvOwners)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchAllOwners()
    }

    // 🔹 Inflate admin menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    // 🔹 Handle logout click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.admin_logout) {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
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
                            ownerType = doc.getString("ownerType") ?: "",
                            phone = doc.getString("serviceProofText") ?: ""
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
        fetchAllOwners()
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
