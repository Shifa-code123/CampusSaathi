package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class OwnerMainActivity : AppCompatActivity(), NavigationHandler {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var tvHeaderTitle: TextView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_main)

        bottomNav = findViewById(R.id.bottom_navv)
        viewPager = findViewById(R.id.viewPagerOwner)
        tvHeaderTitle = findViewById(R.id.ownerHeaderTitle)
        drawerLayout = findViewById(R.id.drawerLayout)

        val adapter = OwnerPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5

        // Setup Side Menu (Drawer)
        val drawerView = findViewById<View>(R.id.customDrawer)
        val drawerHelper = OwnerCustomDrawerHelper(this, drawerLayout, drawerView)
        drawerHelper.setup()

        // Header Buttons
        findViewById<View>(R.id.ownermenuContainer).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.ownernotificationContainer).setOnClickListener {
            startActivity(Intent(this, ActivityOwnerNotification::class.java))
        }

        findViewById<View>(R.id.ownerprofileContainer).setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        // Connect ViewPager2 with BottomNavigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val menuId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_services
                    2 -> R.id.nav_add
                    3 -> R.id.nav_review
                    4 -> R.id.nav_profile
                    else -> R.id.nav_home
                }
                bottomNav.selectedItemId = menuId
                updateHeaderTitle(position)
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_services -> viewPager.currentItem = 1
                R.id.nav_add -> viewPager.currentItem = 2
                R.id.nav_review -> viewPager.currentItem = 3
                R.id.nav_profile -> viewPager.currentItem = 4
            }
            true
        }

        // Set initial state
        updateHeaderTitle(0)

        // 🔥 Handle navigation from other screens (like submission screen)
        val pageIndex = intent.getIntExtra("openPage", -1)

        if (pageIndex != -1) {
            viewPager.post {
                viewPager.setCurrentItem(pageIndex, false)
            }
        }
    }

    private fun updateHeaderTitle(position: Int) {
        tvHeaderTitle.text = when (position) {
            0 -> "Home"
            1 -> "My Services"
            2 -> "Add Service"
            3 -> "Reviews"
            4 -> "Profile"
            else -> "Home"
        }
    }

    override fun navigateTo(tabId: Int) {
        bottomNav.selectedItemId = tabId
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
