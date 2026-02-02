package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // User already logged in → go to LoginActivity
                // LoginActivity will decide dashboard based on role
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // No user logged in
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()

        }, 2000) // 2 seconds splash delay
    }
}
