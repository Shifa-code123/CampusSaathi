package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerVerificationInProgress : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_verification_in_progress)

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc == null) return@addSnapshotListener

                val isVerified = doc.getBoolean("isVerified") ?: false

                if (isVerified) {
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                    finish()
                }
            }
    }
}
