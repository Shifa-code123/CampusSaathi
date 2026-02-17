package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityOwnerSubmissionList1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_submission_list1)

        // Button → Go to Dashboard
        val btnDashboard = findViewById<Button>(R.id.btnDashboard)

        btnDashboard.setOnClickListener {

            val intent = Intent(this, ActivityOwnerDashboard::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}
