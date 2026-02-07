package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // UI references
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnSignup.setOnClickListener {

            // Read inputs
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
            ) {
                toast("All fields are required")
                return@setOnClickListener
            }

            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            // Create Firebase Auth user
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->

                    val uid = authResult.user!!.uid

                    // Firestore user document (CREATED ONLY ONCE)
                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "phone" to phone,

                        // onboarding state
                        "role" to "",                 // student / owner (next screen)
                        "ownerType" to "",            // room_pg / mess / tuition
                        "ownerSetupStep" to 0,
                        "ownerSetupDone" to false,
                        "isVerified" to false,

                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {

                            // Send verification email from CampusSaathi
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    toast(
                                        "Signup successful! Verification email sent to $email"
                                    )
                                }

                            // Move to Role Selection (ONLY ONCE)
                            startActivity(
                                Intent(this, RoleSelectionActivity::class.java)
                            )
                            finish()
                        }
                        .addOnFailureListener {
                            toast("Failed to save user data. Try again.")
                        }
                }
                .addOnFailureListener { exception ->

                    // Handle existing user & other auth errors
                    val errorMessage = when {
                        exception.message?.contains(
                            "email address is already in use",
                            true
                        ) == true ->
                            "This email is already registered. Please login."

                        exception.message?.contains("badly formatted", true) == true ->
                            "Invalid email format."

                        else ->
                            exception.message ?: "Signup failed"
                    }

                    toast(errorMessage)
                }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
