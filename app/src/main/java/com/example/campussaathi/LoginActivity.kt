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
        val txtForgot = findViewById<TextView>(R.id.txtForgotPassword)

        // 🔹 LOGIN BUTTON
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill all fields")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        toast("User not found")
                        return@addOnSuccessListener
                    }

                    checkUserRole(uid)
                }
                .addOnFailureListener {
                    toast("Login failed: ${it.message}")
                }
        }

        // 🔹 SIGNUP
        txtSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 🔹 FORGOT PASSWORD
        txtForgot.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                toast("Enter email first")
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    toast("Password reset email sent")
                }
                .addOnFailureListener {
                    toast(it.message ?: "Error")
                }
        }
    }

    // 🔥 ROLE CHECK LOGIC
    private fun checkUserRole(uid: String) {

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    // New user
                    open(RoleSelectionActivity::class.java)
                    return@addOnSuccessListener
                }

                val role = doc.getString("role")
                val ownerType = doc.getString("ownerType")
                val verificationSubmitted =
                    doc.getBoolean("verificationSubmitted") ?: false
                val isVerified =
                    doc.getBoolean("isVerified") ?: false

                when (role) {

                    "admin" -> open(AdminDashboardActivity::class.java)

                    "student" -> open(StudentDashboardActivity::class.java)

                    "owner" -> {
                        when {
                            ownerType == null ->
                                open(ChooseOwnerType::class.java)

                            !verificationSubmitted ->
                                open(OwnerVerification::class.java)

                            verificationSubmitted && !isVerified ->
                                open(ActivityOwnerVerificationInProgress::class.java)

                            else ->
                                open(ActivityOwnerDashboard::class.java)
                        }
                    }

                    else -> open(RoleSelectionActivity::class.java)
                }
            }
            .addOnFailureListener {
                toast("Database error")
            }
    }

    private fun open(cls: Class<*>) {
        startActivity(Intent(this, cls))
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
