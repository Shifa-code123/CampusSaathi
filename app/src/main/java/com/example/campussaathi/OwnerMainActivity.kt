package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class OwnerMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(OwnerHomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> OwnerHomeFragment()
                R.id.nav_my_services -> MyServicesFragment()
                R.id.nav_add_service -> AddServiceFragment()
                R.id.nav_reviews -> ReviewFragment()
                R.id.nav_profile -> BusinessProfileFragment()
                else -> OwnerHomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}