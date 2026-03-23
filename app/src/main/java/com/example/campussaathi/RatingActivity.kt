package com.example.campussaathi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RatingActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var btnSubmit: Button

    private lateinit var serviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        ratingBar = findViewById(R.id.ratingBar)
        btnSubmit = findViewById(R.id.btnSubmit)

        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""

        btnSubmit.setOnClickListener {

            val rating = ratingBar.rating

            if (rating == 0f) {
                Toast.makeText(this, "Select rating", Toast.LENGTH_SHORT).show()
            } else {
                saveRating(rating)
            }
        }
    }

    private fun saveRating(rating: Float) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val data = hashMapOf(
            "serviceId" to serviceId,
            "studentId" to userId,
            "rating" to rating
        )

        FirebaseFirestore.getInstance()
            .collection("ratings")
            .document(serviceId + "_" + userId) // ✔ one user = one rating
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Rating Submitted ⭐", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}