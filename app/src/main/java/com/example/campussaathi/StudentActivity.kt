package com.example.campussaathi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.utils.DrawerManager
import com.example.campussaathi.utils.ProfileImageLoader
import com.google.android.material.bottomnavigation.BottomNavigationView

class StudentActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var globalHeader: View
    private lateinit var tvHeaderTitle: TextView
    private lateinit var menuContainer: View
    private lateinit var backContainer: View
    private lateinit var ivMenu: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var ivProfile: ImageView
    private lateinit var notificationContainer: View
    private lateinit var profileContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force transparent status bar and dark icons
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_student)

        drawerLayout = findViewById(R.id.studentDrawerLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        viewPager = findViewById(R.id.viewPager)
        globalHeader = findViewById(R.id.globalHeader)
        
        tvHeaderTitle = globalHeader.findViewById(R.id.tvHeaderTitle)
        menuContainer = globalHeader.findViewById(R.id.menuContainer)
        backContainer = globalHeader.findViewById(R.id.backContainer)
        ivMenu = globalHeader.findViewById(R.id.ivMenu)
        ivBack = globalHeader.findViewById(R.id.ivBack)
        ivProfile = globalHeader.findViewById(R.id.ivProfile)
        notificationContainer = globalHeader.findViewById(R.id.notificationContainer)
        profileContainer = globalHeader.findViewById(R.id.profileContainer)

        // Setup Global Drawer
        val drawerView = findViewById<View>(R.id.studentDrawer)
        DrawerManager.setupDrawer(this, drawerLayout, drawerView)
        ProfileImageLoader.loadProfile(ivProfile)

        setupClickListeners()
        
        // Handle back stack changes to restore header state when returning from fragments
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                updateHeader(viewPager.currentItem)
            }
        }

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false 

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_approvals -> viewPager.currentItem = 1
                R.id.nav_users -> viewPager.currentItem = 2
                R.id.nav_services -> viewPager.currentItem = 3
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateHeader(position)
                when (position) {
                    0 -> bottomNavigationView.selectedItemId = R.id.nav_home
                    1 -> bottomNavigationView.selectedItemId = R.id.nav_approvals
                    2 -> bottomNavigationView.selectedItemId = R.id.nav_users
                    3 -> bottomNavigationView.selectedItemId = R.id.nav_services
                }
            }
        })
    }

    private fun setupClickListeners() {
        menuContainer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        backContainer.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        ivProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }
        notificationContainer.setOnClickListener {
            // Handle notifications
        }
    }

    private fun updateHeader(position: Int) {
        // If we have a fragment in container, don't let ViewPager change the header
        if (supportFragmentManager.backStackEntryCount > 0) return

        when (position) {
            0 -> {
                globalHeader.visibility = View.GONE
            }
            1 -> {
                globalHeader.visibility = View.VISIBLE
                tvHeaderTitle.text = "Explore"
                resetHeaderToDefault()
            }
            2 -> {
                globalHeader.visibility = View.VISIBLE
                tvHeaderTitle.text = "Near Me"
                resetHeaderToDefault()
            }
            3 -> {
                globalHeader.visibility = View.VISIBLE
                tvHeaderTitle.text = "Help"
                resetHeaderToDefault()
            }
        }
    }

    private fun resetHeaderToDefault() {
        menuContainer.visibility = View.VISIBLE
        backContainer.visibility = View.GONE
        notificationContainer.visibility = View.VISIBLE
        profileContainer.visibility = View.VISIBLE
    }

    fun updateHeaderForFragment(title: String, isBack: Boolean = false) {
        globalHeader.visibility = View.VISIBLE
        tvHeaderTitle.text = title
        if (isBack) {
            menuContainer.visibility = View.GONE
            backContainer.visibility = View.VISIBLE
            notificationContainer.visibility = View.GONE
            profileContainer.visibility = View.GONE
        } else {
            resetHeaderToDefault()
        }
    }
    
    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }
}