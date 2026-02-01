package com.example.campussaathi;

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView



class ActivityOwnerDashboard : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Toolbar set
        setSupportActionBar(toolbar)

        // Drawer Toggle (hamburger icon)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Menu item clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_add_listing ->
                    Toast.makeText(this, "Add Listing", Toast.LENGTH_SHORT).show()

                R.id.nav_submission ->
                    Toast.makeText(this, "Listing Submission", Toast.LENGTH_SHORT).show()

                R.id.nav_my_listing ->
                    Toast.makeText(this, "My Listings", Toast.LENGTH_SHORT).show()

                R.id.nav_requests ->
                    Toast.makeText(this, "Requests", Toast.LENGTH_SHORT).show()

                R.id.nav_notifications ->
                    Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()

                R.id.nav_profile ->
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
