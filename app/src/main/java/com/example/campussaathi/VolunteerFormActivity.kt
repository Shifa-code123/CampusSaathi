package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VolunteerFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_form)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etReason = findViewById<TextInputEditText>(R.id.etReason)

        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        btnRequest.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val reason = etReason.text.toString().trim()
            val uid = user?.uid

            if (name.isEmpty() || phone.isEmpty() || reason.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "name" to name,
                "phone" to phone,
                "reason" to reason,
                "uid" to uid,
                "status" to "pending"
            )

            db.collection("volunteer_requests")
                .document(uid!!)   // 🔥 important (unique per user)
                .set(data)
                .addOnSuccessListener {

                    Toast.makeText(this, "Request Sent", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, WaitingActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}