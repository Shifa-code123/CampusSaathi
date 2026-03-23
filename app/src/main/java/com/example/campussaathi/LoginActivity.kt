package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        val progressBar = findViewById<ProgressBar>(R.id.progressBar) // 👈 ADD THIS IN XML

        // 🔥 AUTO LOGIN CHECK
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRole(currentUser.uid)
            return
        }

        // 🔹 LOGIN BUTTON
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill all fields")
                return@setOnClickListener
            }

            // 🔥 LOADING START
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        toast("Something went wrong")
                        return@addOnSuccessListener
                    }

                    checkUserRole(uid)
                }
                .addOnFailureListener {

                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true

                    val message = when {
                        it.message?.contains("badly formatted") == true ->
                            "Invalid email format"
                        it.message?.contains("password is invalid") == true ->
                            "Wrong password"
                        it.message?.contains("no user record") == true ->
                            "User not found"
                        else -> "Login failed"
                    }

                    toast(message)
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
                .addOnSuccessListener { toast("Password reset email sent") }
                .addOnFailureListener { toast(it.message ?: "Error") }
        }
    }

    // 🔥 ROLE + VERIFICATION LOGIC (UNCHANGED CORE)
    private fun checkUserRole(uid: String) {

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    open(RoleSelectionActivity::class.java)
                    return@addOnSuccessListener
                }

                val role = doc.getString("role")
                val ownerType = doc.getString("ownerType")

                when (role) {

                    "admin" -> open(AdminActivityMain::class.java)


                    "student" -> open(StudentActivity::class.java)

                    "owner" -> {

                        if (ownerType == null) {
                            open(ChooseOwnerType::class.java)
                            return@addOnSuccessListener
                        }

                        db.collection("owner_verifications")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { verificationDoc ->

                                if (!verificationDoc.exists()) {
                                    open(OwnerVerification::class.java)
                                    return@addOnSuccessListener
                                }

                                val status =
                                    verificationDoc.getString("status") ?: "pending"

                                when (status) {

                                    "pending" ->
                                        open(ActivityOwnerVerificationInProgress::class.java)

                                    "approved" ->
                                        open(OwnerMainActivity::class.java)

                                    "rejected" ->
                                        open(OwnerVerification::class.java)

                                    else ->
                                        open(OwnerVerification::class.java)
                                }
                            }
                    }

                    else -> open(RoleSelectionActivity::class.java)
                }

                // 🔥 STOP LOADING AFTER ROLE CHECK
                stopLoading()
            }
            .addOnFailureListener {
                toast("Database error")
                stopLoading()
            }
    }

    private fun open(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun stopLoading() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        progressBar.visibility = View.GONE
        btnLogin.isEnabled = true
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}