package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class ActivityOwnerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> {
                    startActivity(
                        Intent(this, ActivityOwnerAddNewList1::class.java)
                    )
                }

                R.id.nav_submission -> {
                    startActivity(
                        Intent(this, ActivityOwnerSubmissionList1::class.java)
                    )
                }

                R.id.nav_my_listing -> {
                    startActivity(
                        Intent(this, ActivityOwnerMyList::class.java)
                    )
                }

                R.id.nav_requests -> {
                    Toast.makeText(this, "Requests – Coming Soon", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications – Coming Soon", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, ActivityOwnerProfile::class.java)
                    )
                }

                R.id.nav_logout -> {
                    finish() // ya logout logic
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
