package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

        // Drawer Menu Click
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
<<<<<<< HEAD
=======

        // Notification Click
        binding.Header.ivNotification.setOnClickListener {
            // Open notification screen
        }

        // Profile Click
        binding.Header.ivProfile.setOnClickListener {
            // Open profile screen
        }
        Log.d("CHECK_ACTIVITY", "StudentDashboardActivity OPENED")

         val home = findViewById<View>(R.id.cs_footer_home_container)
        val explore = findViewById<View>(R.id.cs_footer_explore_container)
        val nearme = findViewById<View>(R.id.cs_footer_nearme_container)
        val help = findViewById<View>(R.id.cs_footer_help_container)

        val homeIcon = findViewById<ImageView>(R.id.cs_footer_home_icon)
        val exploreIcon = findViewById<ImageView>(R.id.cs_footer_explore_icon)
        val nearmeIcon = findViewById<ImageView>(R.id.cs_footer_nearme_icon)
        val helpIcon = findViewById<ImageView>(R.id.cs_footer_help_icon)

        val homeText = findViewById<TextView>(R.id.cs_footer_home_text)
        val exploreText = findViewById<TextView>(R.id.cs_footer_explore_text)
        val nearmeText = findViewById<TextView>(R.id.cs_footer_nearme_text)
        val helpText = findViewById<TextView>(R.id.cs_footer_help_text)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuIcon = findViewById<ImageView>(R.id.ivMenu)



        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)


        }

        fun resetSelection() {

            val defaultColor = getColor(R.color.cs_footer_default)

            home.setBackgroundResource(0)
            explore.setBackgroundResource(0)
            nearme.setBackgroundResource(0)
            help.setBackgroundResource(0)

            homeIcon.setColorFilter(defaultColor)
            exploreIcon.setColorFilter(defaultColor)
            nearmeIcon.setColorFilter(defaultColor)
            helpIcon.setColorFilter(defaultColor)

            homeText.setTextColor(defaultColor)
            exploreText.setTextColor(defaultColor)
            nearmeText.setTextColor(defaultColor)
            helpText.setTextColor(defaultColor)
        }


        home.setOnClickListener {
            resetSelection()
            selectItem(home, homeIcon, homeText)
        }

        explore.setOnClickListener {
            resetSelection()
            selectItem(explore, exploreIcon, exploreText)
        }

        nearme.setOnClickListener {
            resetSelection()
            selectItem(nearme, nearmeIcon, nearmeText)
        }

        help.setOnClickListener {
            resetSelection()
            selectItem(help, helpIcon, helpText)

            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

// Default selected item
        selectItem(home, homeIcon, homeText)
>>>>>>> 89008c208722c41ca59c3efbe0be7d0278d1b96b
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
        }

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            if (selectedTab != "help") {
                startActivity(Intent(this, HelpActivity::class.java))
            }
        }*/
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