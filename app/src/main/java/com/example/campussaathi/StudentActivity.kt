package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class StudentActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        viewPager = findViewById(R.id.viewPager)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        // Bottom navigation click → change page
        bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_approvals -> viewPager.currentItem = 1
                R.id.nav_users -> viewPager.currentItem = 2
                R.id.nav_services -> viewPager.currentItem = 3
            }

            true
        }

        // Swipe → update bottom navigation
        viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {

                when (position) {

                    0 -> bottomNavigationView.selectedItemId = R.id.nav_home
                    1 -> bottomNavigationView.selectedItemId = R.id.nav_approvals
                    2 -> bottomNavigationView.selectedItemId = R.id.nav_users
                    3 -> bottomNavigationView.selectedItemId = R.id.nav_services
                }
            }
        })
    }
}