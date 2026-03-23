package com.example.campussaathi
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.campussaathi.databinding.AdminActivityManageBinding

class AdminManageActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityManageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type") ?: ""
        setupHeader(type)
        loadFragment(type)
    }

    private fun setupHeader(type: String) {
        val title = when (type) {
            "users" -> "Manage Students"
            "owners" -> "Manage Owners"
            "volunteers" -> "Manage Volunteers"
            "cityhelp" -> "Manage CityHelp"
            else -> "Manage"
        }
        
        binding.adminManageHeader.apply {
            adminHeaderTitle.text = title
            
            // Show back button for Manage Activity
            adminBackBtn.visibility = View.VISIBLE
            adminBackBtn.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            
            // Notification and Profile Icons
            adminNotificationBtn.setOnClickListener {
                startActivity(Intent(this@AdminManageActivity, AdminNotificationActivity::class.java))
            }
            adminProfileBtn.setOnClickListener {
                startActivity(Intent(this@AdminManageActivity, AdminProfileActivity::class.java))
            }
            
            adminLogoLayout.setOnClickListener {
                finish()
            }
        }
    }

    private fun loadFragment(type: String) {
        val fragment: Fragment = when (type) {
            "users" -> AdminManageUsersFragment()
            "owners" -> AdminManageOwnersFragment()
            "volunteers" -> AdminManageVolunteersFragment()
            "cityhelp" -> AdminManageCityHelpFragment()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.adminManageContainer, fragment)
            .commit()
    }
}
