package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VolunteerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_dashboard)

        val btnAddCity = findViewById<MaterialButton>(R.id.btnAddCity)
        findViewById<View>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAddCity.setOnClickListener {
            startActivity(Intent(this, ActivityCityHelp::class.java))
        }
    }
}