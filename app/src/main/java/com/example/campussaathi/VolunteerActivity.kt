package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VolunteerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)

        val btnApply = findViewById<MaterialButton>(R.id.btnApply)

        // 🔥 FIREBASE INIT
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // 🔥 STATUS CHECK FIRST
        uid?.let {

            db.collection("volunteer_requests")
                .document(it)
                .get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        val status = doc.getString("status")

                        when (status) {

                            "pending" -> {
                                startActivity(Intent(this, WaitingActivity::class.java))
                                finish()
                            }

                            "approved" -> {
                                startActivity(Intent(this, VolunteerDashboardActivity::class.java))
                                finish()
                            }

                            "blocked" -> {
                                Toast.makeText(this, "You are blocked by admin", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    } else {
                        // ✅ NEW USER → ALLOW APPLY
                        btnApply.setOnClickListener {
                            startActivity(Intent(this, VolunteerFormActivity::class.java))
                        }
                    }
                }
        }

        // 🔙 BACK BUTTON
        findViewById<View>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}