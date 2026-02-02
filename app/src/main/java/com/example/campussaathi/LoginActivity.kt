package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java
import kotlin.text.trim

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

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser!!.uid

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->

                            val role = doc.getString("role")

                            if (role == "student") {
                                startActivity(
                                    Intent(
                                        this,
                                        StudentDashboardActivity::class.java
                                    )
                                )
                            } else if (role == "owner") {
                                startActivity(
                                    Intent(
                                        this,
                                        ActivityOwnerDashboard::class.java
                                    )
                                )
                            } else {
                                // role not selected yet
                                startActivity(
                                    Intent(
                                        this,
                                        RoleSelectionActivity::class.java
                                    )
                                )
                            }
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
        }

        txtSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
