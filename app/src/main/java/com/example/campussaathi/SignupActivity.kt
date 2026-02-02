package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlin.jvm.java
import kotlin.text.isEmpty
import kotlin.text.trim
import kotlin.to

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnSignup.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val uid = result.user!!.uid

                    val userMap = hashMapOf(
                        "fullName" to name,
                        "email" to email,
                        "phone" to phone,
                        "role" to "", // role will be set next
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            // Move to role selection
                            startActivity(Intent(this, RoleSelectionActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }
}
