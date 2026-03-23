package com.example.campussaathi

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.campussaathi.databinding.AdminActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdminHeader(
            headerBinding = binding.adminProfileHeader,
            title = "My Profile",
            showBack = true
        )

        loadProfile()
        binding.btnAdminLogout.setOnClickListener { showLogoutDialog() }
        binding.btnToggleTheme.setOnClickListener { toggleTheme() }
    }

    private fun loadProfile() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvAdminProfileEmail.text = user.email
            
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.tvAdminProfileName.text = document.getString("name") ?: "Admin"
                        binding.tvAdminProfilePhone.text = document.getString("phone") ?: "N/A"
                    }
                }
        }
    }

    private fun toggleTheme() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val newMode = if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        
        AppCompatDelegate.setDefaultNightMode(newMode)
        
        // Save preference
        val sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("NightMode", newMode)
            apply()
        }
        
        Toast.makeText(this, "Theme Switched", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}