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
        uid = intent.getStringExtra("uid") ?: return

        val txtDetails = findViewById<TextView>(R.id.txtDetails)
        val btnApprove = findViewById<Button>(R.id.btnApprove)
        val btnReject = findViewById<Button>(R.id.btnReject)

        // LOAD DATA
        db.collection("owner_verifications").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    txtDetails.text =
                        "Owner Type: ${doc.getString("ownerType")}\n" +
                                "ID Proof: ${doc.getString("idProofText")}\n" +
                                "Service Proof: ${doc.getString("serviceProofText")}"
                }
            }

        btnApprove.setOnClickListener {
            approveOwner()
        }

        btnReject.setOnClickListener {
            rejectOwner()
        }
    }

    private fun approveOwner() {
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
        finish()
    }

    private fun rejectOwner() {
        db.collection("owner_verifications").document(uid)
            .update("status", "rejected")

        db.collection("users").document(uid)
            .update("isVerified", false)

        Toast.makeText(this, "Owner Rejected", Toast.LENGTH_SHORT).show()
        finish()
    }
}
