package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivitySupportBinding
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupDrawer()
        setupFooter("help")
        setupActions()
    }

    // ---------------- HEADER ----------------

    private fun setupHeader() {
        binding.header.tvHeaderTitle.text = "Help & Support"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // ---------------- DRAWER ----------------

    private fun setupDrawer() {

        val drawerView = binding.drawerLayout.getChildAt(1)

        drawerView.findViewById<View>(R.id.menuHome).setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        /*drawerView.findViewById<View>(R.id.menuProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerView.findViewById<View>(R.id.menuSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }*/

        drawerView.findViewById<View>(R.id.menuHelp).setOnClickListener {
            // Already here
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerView.findViewById<View>(R.id.menuLogout).setOnClickListener {
            // logout logic
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // ---------------- FOOTER ----------------

    private fun setupFooter(selectedTab: String) {

        val defaultColor = ContextCompat.getColor(this, R.color.cs_footer_default)
        val selectedColor = ContextCompat.getColor(this, R.color.cs_footer_selected_icon)

        fun resetSelection() {
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
            container.setBackgroundResource(R.drawable.cs_footer_bg_selected)
            icon.setColorFilter(selectedColor)
            text.setTextColor(selectedColor)
        }

        resetSelection()

        if (selectedTab == "help") {
            selectItem(
                binding.csFooter.csFooterHelpContainer,
                binding.csFooter.csFooterHelpIcon,
                binding.csFooter.csFooterHelpText
            )
        }

        binding.csFooter.csFooterHomeContainer.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        /*binding.csFooter.csFooterNearmeContainer.setOnClickListener {
            startActivity(Intent(this, NearMeActivity::class.java))
        }*/

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            // Already here
        }
    }

    // ---------------- BUTTON ACTIONS ----------------

    private fun setupActions() {

        // ---------------- SUBMIT BUTTON INITIAL STATE ----------------

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.5f

        // Enable only when user types
        binding.etIssue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString()?.trim()

                if (!text.isNullOrEmpty()) {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.alpha = 1f
                } else {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.alpha = 0.5f
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // ---------------- SUBMIT CLICK ----------------

        binding.btnSubmit.setOnClickListener {

            val issueText = binding.etIssue.text.toString().trim()

            if (issueText.isNotEmpty()) {

                binding.etIssue.text?.clear()
                binding.btnSubmit.isEnabled = false
                binding.btnSubmit.alpha = 0.5f

                // ✅ Toast Message
                android.widget.Toast.makeText(
                    this,
                    "Issue submitted successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }


        // ---------------- CALL BUTTON ----------------

        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:9876543210")
            startActivity(intent)
        }

        // ---------------- WHATSAPP ----------------

        binding.btnMessage.setOnClickListener {
            val phoneNumber = "919876543210"
            val url = "https://wa.me/$phoneNumber"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}