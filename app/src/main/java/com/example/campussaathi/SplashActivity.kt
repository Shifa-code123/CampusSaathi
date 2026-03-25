package com.example.campussaathi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 🔥 SPLASH DELAY
        Handler(Looper.getMainLooper()).postDelayed({
            startAppFlow()
        }, 1500)
    }

    private fun startAppFlow() {

        // ✅ STEP 1: Onboarding check
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            open(OnboardingActivity::class.java)
            return
        }

        val currentUser = auth.currentUser

        // 🔹 NOT LOGGED IN
        if (currentUser == null) {
            open(LoginActivity::class.java)
            return
        }

        // ✅ STEP 2: Location check (New requirement)
        val prefs = getSharedPreferences("CampusSaathiPrefs", Context.MODE_PRIVATE)
        val city = prefs.getString("selected_city", null)
        val college = prefs.getString("selected_college", null)

        if (city == null || college == null) {
            open(SelectLocationActivity::class.java)
            return
        }

        val uid = currentUser.uid

        // 🔹 FETCH USER ROLE
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    open(RoleSelectionActivity::class.java)
                    return@addOnSuccessListener
                }

                val role = doc.getString("role")
                val ownerSetupDone = doc.getBoolean("ownerSetupDone") ?: false
                val isVerified = doc.getBoolean("isVerified") ?: false
                val verificationSubmitted = doc.getBoolean("verificationSubmitted") ?: false

                when (role) {
                    "admin" -> open(AdminActivityMain::class.java)
                    "student" -> open(StudentActivity::class.java)
                    "owner" -> {
                        when {
                            !ownerSetupDone -> open(ActivityOwnerChooseTypeService::class.java)
                            !verificationSubmitted -> open(OwnerVerification::class.java)
                            !isVerified -> open(ActivityOwnerVerificationInProgress::class.java)
                            else -> open(OwnerMainActivity::class.java)
                        }
                    }
                    else -> open(RoleSelectionActivity::class.java)
                }
            }
            .addOnFailureListener {
                open(LoginActivity::class.java)
            }
    }

    private fun open(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
