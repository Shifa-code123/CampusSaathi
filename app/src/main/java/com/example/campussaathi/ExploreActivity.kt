package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.ActivityExploreBinding
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var originalList: List<CategoryModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Header Title
        binding.header.tvHeaderTitle.text = "Explore"

        // Drawer Open
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Notification Click
        binding.header.ivNotification.setOnClickListener {
            // Open notification screen
        }

        // Profile Click
        binding.header.ivProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }

        setupRecycler()
        setupSearch()
        setupBottomNav()
        setupDrawer()
    }

    // ---------------- RECYCLER ----------------

    private fun setupRecycler() {

        originalList = listOf(
            CategoryModel("Mess", R.drawable.img_cat_mess),
            CategoryModel("Room", R.drawable.img_cat_room),
            CategoryModel("Tuition", R.drawable.img_cat_tuition),
            CategoryModel("Street Food", R.drawable.img_cat_streetfood),
            CategoryModel("Medical", R.drawable.img_cat_medical),
            CategoryModel("Stationery", R.drawable.img_cat_stationary),
            CategoryModel("Fitness", R.drawable.img_cat_fitness),
            CategoryModel("College Services", R.drawable.img_cat_collegeservices)
        )

        binding.rvCategories.layoutManager = GridLayoutManager(this, 2)
        categoryAdapter = CategoryAdapter(originalList)
        binding.rvCategories.adapter = categoryAdapter
    }

    // ---------------- SEARCH ----------------

    private fun setupSearch() {

        binding.edtSearch.addTextChangedListener { editable ->

            val query = editable.toString().trim().lowercase()

            val filteredList = originalList.filter {
                it.name.lowercase().contains(query)
            }

            categoryAdapter.updateList(filteredList)
        }
    }

    // ---------------- DRAWER ----------------

    private fun setupDrawer() {

        binding.studentDrawer.menuHome.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutDialog()
        }
    }

    // ---------------- LOGOUT ----------------

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

    // ---------------- FOOTER ----------------

    private fun setupBottomNav() {

        binding.studentBottomNav.selectedItemId = R.id.nav_explore

        binding.studentBottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, StudentDashboardActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }

                R.id.nav_explore -> {
                    true
                }

                R.id.nav_nearby -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }

                R.id.nav_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }

                else -> false
            }
        }
    }
}