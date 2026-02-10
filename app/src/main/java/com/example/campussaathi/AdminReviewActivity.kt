package com.example.campussaathi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminReviewActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_review)

        db = FirebaseFirestore.getInstance()

        val txtDetails = findViewById<TextView>(R.id.txtDetails)
        val btnApprove = findViewById<Button>(R.id.btnApprove)
        val btnReject = findViewById<Button>(R.id.btnReject)

        // 🔴 OWNER UID RECEIVE KARNA
        uid = intent.getStringExtra("uid") ?: run {
            Toast.makeText(this, "Owner UID not received", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 🔥 OWNER BASIC INFO LOAD (users collection se)
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val name = doc.getString("fullName")
                    val phone = doc.getString("phone")
                    val ownerType = doc.getString("ownerType")

                    txtDetails.text =
                        "Name: $name\n" +
                                "Phone: $phone\n\n" +
                                "Owner Type: $ownerType"

                } else {
                    txtDetails.text = "Owner data not found"
                }
            }
            .addOnFailureListener {
                txtDetails.text = "Error loading owner data"
            }

        btnApprove.setOnClickListener { approveOwner() }
        btnReject.setOnClickListener { rejectOwner() }
    }

    // ✅ APPROVE OWNER
    private fun approveOwner() {

        // 1️⃣ verification table update
        db.collection("owner_verifications").document(uid)
            .update("status", "approved")

        // 2️⃣ user profile update
        db.collection("users").document(uid)
            .update(
                mapOf(
                    "isVerified" to true,
                    "verificationSubmitted" to true
                )
            )

        Toast.makeText(this, "Owner Approved", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ❌ REJECT OWNER
    private fun rejectOwner() {

        db.collection("owner_verifications").document(uid)
            .update("status", "rejected")

        db.collection("users").document(uid)
            .update("isVerified", false)

        Toast.makeText(this, "Owner Rejected", Toast.LENGTH_SHORT).show()
        finish()
    }
}
