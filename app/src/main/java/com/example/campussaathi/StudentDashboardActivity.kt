package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityStudentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Header Title
        binding.header.tvHeaderTitle.text = "Home"

        // Drawer Setup
        setupDrawer()

        // Footer Setup
        setupFooter("home")

        // Drawer Open
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Notification Click (Friend's logic kept)
        binding.header.ivNotification.setOnClickListener {
            // Open notification screen
        }

        // Profile Click (Friend's logic kept)
        binding.header.ivProfile.setOnClickListener {
            // Open profile screen
        }

        Log.d("CHECK_ACTIVITY", "StudentDashboardActivity OPENED")
    }

    // ---------------- FOOTER ----------------

    private fun setupFooter(selectedTab: String) {

        binding.csFooter.csFooterHomeContainer.setOnClickListener {
            if (selectedTab != "home") {
                startActivity(Intent(this, StudentDashboardActivity::class.java))
            }
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {
            if (selectedTab != "explore") {
                startActivity(Intent(this, ExploreActivity::class.java))
            }
        }

        /*binding.csFooter.csFooterNearmeContainer.setOnClickListener {
            if (selectedTab != "near") {
                startActivity(Intent(this, NearMeActivity::class.java))
            }
        }*/

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            if (selectedTab != "help") {
                startActivity(Intent(this, HelpActivity::class.java))
            }
        }
    }

    // ---------------- DRAWER ----------------

    private fun setupDrawer() {
        binding.studentDrawer.menuLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Log out of your account?")
            .setPositiveButton("Log Out") { _, _ ->

                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}