package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.databinding.AdminActivityMainBinding

class AdminActivityMain : AppCompatActivity() {

    lateinit var binding: AdminActivityMainBinding
    private lateinit var adapter: AdminViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status Bar Visibility Fix
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = AdminActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupViewPager()
        setupNavigation()
    }

    private fun setupHeader() {
        setupAdminHeader(
            headerBinding = binding.adminHeader,
            title = "Admin Dashboard",
            showBack = false,
            onLogoClick = {
                if (binding.adminViewPager.currentItem != 0) {
                    setCurrentPage(0)
                }
            }
        )
    }

    private fun setHeaderTitle(title: String) {
        binding.adminHeader.adminHeaderTitle.text = title
    }

    fun setCurrentPage(position: Int) {
        binding.adminViewPager.currentItem = position
    }

    private fun setupViewPager() {
        adapter = AdminViewPagerAdapter(this)
        binding.adminViewPager.adapter = adapter

        // Optimize ViewPager performance
        binding.adminViewPager.offscreenPageLimit = 3

        // Sync ViewPager with Bottom Navigation
        binding.adminViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menu = binding.adminBottomNavigation.menu
                when (position) {
                    0 -> {
                        menu.findItem(R.id.nav_home).isChecked = true
                        setHeaderTitle("Admin Dashboard")
                    }
                    1 -> {
                        menu.findItem(R.id.nav_approvals).isChecked = true
                        setHeaderTitle("Pending Approvals")
                    }
                    2 -> {
                        menu.findItem(R.id.nav_volunteers).isChecked = true
                        setHeaderTitle("Volunteers List")
                    }
                    3 -> {
                        menu.findItem(R.id.nav_services).isChecked = true
                        setHeaderTitle("Services Management")
                    }
                }
            }
        })
    }

    private fun setupNavigation() {
        binding.adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    setCurrentPage(0)
                    true
                }
                R.id.nav_approvals -> {
                    setCurrentPage(1)
                    true
                }
                R.id.nav_volunteers -> {
                    setCurrentPage(2)
                    true
                }
                R.id.nav_services -> {
                    setCurrentPage(3)
                    true
                }
                else -> false
            }
        }
    }
}
