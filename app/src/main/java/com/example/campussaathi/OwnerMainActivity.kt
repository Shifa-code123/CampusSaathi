package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment

class OwnerMainActivity : AppCompatActivity(), NavigationHandler {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_main)

        bottomNav = findViewById(R.id.bottom_navv)

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    loadFragment(OwnerHomeFragment())
                    true
                }

                R.id.nav_services -> {
                    loadFragment(MyServicesFragment())
                    true
                }

                R.id.nav_add -> {
                    loadFragment(AddServiceFragment())
                    true
                }

                R.id.nav_review -> {
                    loadFragment(ReviewFragment())
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(BusinessProfileFragment())
                    true
                }

                else -> false
            }
        }

        // 🔥 THIS LINE LOADS HOME PROPERLY
        bottomNav.selectedItemId = R.id.nav_home
    }

    override fun navigateTo(tabId: Int) {
        bottomNav.selectedItemId = tabId
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}