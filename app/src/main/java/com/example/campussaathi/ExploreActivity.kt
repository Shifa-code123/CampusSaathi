package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campussaathi.databinding.ActivityExploreBinding

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

    // ---------------- FOOTER ----------------

    private fun setupFooter(selectedTab: String) {

        binding.csFooter.csFooterHomeContainer.setOnClickListener {
            if (selectedTab != "home") {
                startActivity(Intent(this, StudentDashboardActivity::class.java))
                finish()
            }
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {
            // Already on explore
        }

        /*
        binding.csFooter.csFooterNearmeContainer.setOnClickListener {
            startActivity(Intent(this, NearMeActivity::class.java))
            finish()
        }

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
            finish()
        }
        */
    }
}