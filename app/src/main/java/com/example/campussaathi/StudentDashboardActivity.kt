package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.ActivityStudentDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.HashMap

class StudentDashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityStudentDashboardBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.tvHeaderTitle.text = "Home"

        setupDrawer()
        setupBottomNav()

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        Log.d("CHECK_ACTIVITY", "StudentDashboardActivity OPENED")

        binding.servicesRecycler.layoutManager = LinearLayoutManager(this)

        loadServices()

        setupMap()
    }

    // ---------------- MAP SETUP ----------------

    private fun setupMap() {

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        val khamgaon = LatLng(20.706344, 76.572811)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(khamgaon, 14f))

        loadNearbyPlaces()

        mMap.setOnMarkerClickListener { marker ->

            val tag = marker.tag as HashMap<String, String>

            binding.placeName.text = tag["name"]
            binding.placeAddress.text = tag["address"]

            binding.placeCard.visibility = View.VISIBLE

            false
        }
    }

    private fun loadNearbyPlaces() {

        db.collection("nearby_places")
            .get()
            .addOnSuccessListener { documents ->

                for (doc in documents) {

                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")
                    val name = doc.getString("name")
                    val address = doc.getString("address")

                    if (lat != null && lng != null) {

                        val location = LatLng(lat, lng)

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(name)
                        )

                        val data = HashMap<String, String>()

                        data["name"] = name ?: ""
                        data["address"] = address ?: ""

                        marker?.tag = data
                    }
                }
            }
    }

    // ---------------- SERVICES ----------------

    private fun loadServices() {

        db.collection("services")
            .get()
            .addOnSuccessListener { docs ->

                val servicesList = ArrayList<Service>()

                for (doc in docs) {

                    val ownerId = doc.getString("ownerId") ?: ""
                    val photos = doc.get("photos") as? List<String> ?: ArrayList()
                    val serviceName = doc.getString("serviceName") ?: ""

                    val latitude = doc.getDouble("latitude") ?: 0.0
                    val longitude = doc.getDouble("longitude") ?: 0.0
                    val phone = doc.get("contact")?.toString() ?: ""
                    val description = doc.getString("description") ?: ""

                    servicesList.add(
                        Service(
                            ownerId,
                            photos,
                            serviceName,
                            latitude,
                            longitude,
                            phone,
                            description
                        )
                    )
                }

                binding.servicesRecycler.adapter = ServicesAdapter(servicesList)
            }
    }

    // ---------------- BOTTOM NAV ----------------

    private fun setupBottomNav() {

        binding.studentBottomNav.selectedItemId = R.id.nav_home

        binding.studentBottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> true

                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }

               // R.id.nav_nearby -> {
                   // startActivity(Intent(this, NearbyActivity::class.java))
                    //overridePendingTransition(0,0)
                    //true
                //}

                R.id.nav_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }

                else -> false
            }
        }
    }

    // ---------------- DRAWER ----------------

    private fun setupDrawer() {

        binding.studentDrawer.menuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }

        binding.studentDrawer.menuHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        binding.studentDrawer.menuLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Log out of your account?")
            .setPositiveButton("Log Out") { _, _ ->

                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}