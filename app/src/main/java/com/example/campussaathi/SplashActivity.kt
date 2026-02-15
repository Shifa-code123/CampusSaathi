package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUser = auth.currentUser

            // 🔹 If user NOT logged in → go to Login
            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@postDelayed
            }

            // 🔹 If user IS logged in → check role from Firestore
            val uid = currentUser.uid

            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->

                    val role = doc.getString("role")
                    val ownerSetupDone = doc.getBoolean("ownerSetupDone") ?: false
                    val isVerified = doc.getBoolean("isVerified") ?: false
                    val verificationSubmitted = doc.getBoolean("verificationSubmitted") ?: false

                    when (role) {

                        "student" -> {
                            startActivity(
                                Intent(this, StudentDashboardActivity::class.java)
                            )
                        }

                        "owner" -> {
                            when {
                                !ownerSetupDone -> {
                                    startActivity(
                                        Intent(this, ChooseOwnerType::class.java)
                                    )
                                }
                                !verificationSubmitted -> {
                                    startActivity(
                                        Intent(this, OwnerVerification::class.java)
                                    )
                                }
                                !isVerified -> {
                                    startActivity(
                                        Intent(this, ActivityOwnerVerificationInProgress::class.java)
                                    )
                                }
                                else -> {
                                    startActivity(
                                        Intent(this, ActivityOwnerDashboard::class.java)
                                    )
                                }
                            }
                        }

                        else -> {
                            startActivity(
                                Intent(this, RoleSelectionActivity::class.java)
                            )
                        }
                    }

                    finish()
                }

        }, 2000) // 2 second splash
    }
}
