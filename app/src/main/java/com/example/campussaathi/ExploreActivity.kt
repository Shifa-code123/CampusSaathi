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
import androidx.appcompat.app.AlertDialog

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

        // Setup Recycler
        setupRecycler()

        // Setup Search
        setupSearch()

        // Setup Footer
        setupFooter("explore")
        // Drawer Open
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

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

        binding.studentDrawer.menuHelp.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuLogout.setOnClickListener {
            showLogoutDialog()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

// ---------------- LOGOUT DIALOG ----------------

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
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
}