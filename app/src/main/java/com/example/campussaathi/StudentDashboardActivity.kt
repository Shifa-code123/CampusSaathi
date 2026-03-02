package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityStudentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.View
import android.widget.ImageView
import android.widget.TextView


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

        fun resetSelection() {
            val defaultColor = getColor(R.color.cs_footer_default)

            binding.csFooter.csFooterHomeContainer.setBackgroundResource(0)
            binding.csFooter.csFooterExploreContainer.setBackgroundResource(0)
            binding.csFooter.csFooterNearmeContainer.setBackgroundResource(0)
            binding.csFooter.csFooterHelpContainer.setBackgroundResource(0)

            binding.csFooter.csFooterHomeIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterExploreIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterNearmeIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterHelpIcon.setColorFilter(defaultColor)

            binding.csFooter.csFooterHomeText.setTextColor(defaultColor)
            binding.csFooter.csFooterExploreText.setTextColor(defaultColor)
            binding.csFooter.csFooterNearmeText.setTextColor(defaultColor)
            binding.csFooter.csFooterHelpText.setTextColor(defaultColor)
        }

        fun selectItem(container: View, icon: ImageView, text: TextView) {
            val selectedColor = getColor(R.color.cs_footer_selected_icon)

            container.setBackgroundResource(R.drawable.cs_footer_bg_selected)
            icon.setColorFilter(selectedColor)
            text.setTextColor(selectedColor)

            container.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(120)
                .withEndAction {
                    container.animate().scaleX(1f).scaleY(1f).duration = 80
                }
        }

        // Default selection
        when (selectedTab) {
            "home" -> selectItem(
                binding.csFooter.csFooterHomeContainer,
                binding.csFooter.csFooterHomeIcon,
                binding.csFooter.csFooterHomeText
            )
            "explore" -> selectItem(
                binding.csFooter.csFooterExploreContainer,
                binding.csFooter.csFooterExploreIcon,
                binding.csFooter.csFooterExploreText
            )
            "near" -> selectItem(
                binding.csFooter.csFooterNearmeContainer,
                binding.csFooter.csFooterNearmeIcon,
                binding.csFooter.csFooterNearmeText
            )
            "help" -> selectItem(
                binding.csFooter.csFooterHelpContainer,
                binding.csFooter.csFooterHelpIcon,
                binding.csFooter.csFooterHelpText
            )
        }

        // Click listeners
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