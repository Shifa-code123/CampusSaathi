package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btn = findViewById<Button>(R.id.btnOpenVerification)

        btn.setOnClickListener {
            // OPEN REVIEW DIRECTLY WITH UID
            val intent = Intent(this, AdminReviewActivity::class.java)
            intent.putExtra("uid", "VNUaMfEZd8Sc1QxvLjRSDuegK2") // YOUR TEST UID
            startActivity(intent)
        }
    }
}
