package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtSignup = findViewById<TextView>(R.id.txtSignup)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Fill all fields")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->

                            val role = doc.getString("role")
                            val ownerType = doc.getString("ownerType")
                            val verificationSubmitted =
                                doc.getBoolean("verificationSubmitted") ?: false
                            val isVerified =
                                doc.getBoolean("isVerified") ?: false

                            when (role) {

                                "admin" -> {
                                    startActivity(
                                        Intent(this, AdminDashboardActivity::class.java)
                                    )
                                    finish()
                                }

                                null -> {
                                    startActivity(
                                        Intent(this, RoleSelectionActivity::class.java)
                                    )
                                }

                                "student" -> {
                                    startActivity(
                                        Intent(this, StudentDashboardActivity::class.java)
                                    )
                                }

                                "owner" -> {

                                    when {
                                        ownerType == null -> {
                                            startActivity(
                                                Intent(this, ChooseOwnerType::class.java)
                                            )
                                        }

                                        !verificationSubmitted -> {
                                            startActivity(
                                                Intent(this, OwnerVerification::class.java)
                                            )
                                        }

                                        verificationSubmitted && !isVerified -> {
                                            startActivity(
                                                Intent(
                                                    this,
                                                    ActivityOwnerVerificationInProgress::class.java
                                                )
                                            )
                                        }

                                        verificationSubmitted && isVerified -> {
                                            startActivity(
                                                Intent(
                                                    this,
                                                    ActivityOwnerDashboard::class.java
                                                )
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    // fallback safety
                                    startActivity(
                                        Intent(this, RoleSelectionActivity::class.java)
                                    )
                                }
                            }

                            finish()
                        }
                }
                .addOnFailureListener {
                    showToast("Login failed: ${it.message}")
                }
        }

        txtSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
