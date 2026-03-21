package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityOwnerSubmissionList1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_submission_list1)

        val btnDashboard = findViewById<Button>(R.id.btnDashboard)
        val btnAddAnother = findViewById<Button>(R.id.addanotherservice)

        // 👉 Dashboard → ViewPager2 index 0 (OwnerHomeFragment)
        btnDashboard.setOnClickListener {

            val intent = Intent(this, OwnerMainActivity::class.java)
            intent.putExtra("openPage", 0) // 👈 IMPORTANT

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        // 👉 Add Service → ViewPager2 index (maan le 1 hai)
        btnAddAnother.setOnClickListener {

            val intent = Intent(this, OwnerMainActivity::class.java)
            intent.putExtra("openPage", 2) // 👈 AddServiceFragment index

            startActivity(intent)
            finish()
        }
    }

    // 🔥 Back disable (force screen)
    override fun onBackPressed() {
        // do nothing
    }
}