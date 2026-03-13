package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.MotionEvent
import com.example.campussaathi.utils.DrawerManager

class DashboardFragment : Fragment(), OnMapReadyCallback {


    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        DrawerManager.setupDrawer(
            requireActivity(),
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.drawerLayout.setDrawerLockMode(
            androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
        )

        binding.drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT)

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.mapContainer.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    binding.servicesRecycler.parent
                        .requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.servicesRecycler.parent
                        .requestDisallowInterceptTouchEvent(false)
                }
            }

            false
        }

        binding.servicesRecycler.layoutManager =
            LinearLayoutManager(requireContext())



        loadServices()
        setupMap()

        // MAP TOUCH → disable tab swipe
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.view?.setOnTouchListener { _, event ->

            val viewPager =
                requireActivity().findViewById<ViewPager2>(R.id.viewPager)

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    viewPager.isUserInputEnabled = false
                }

                MotionEvent.ACTION_UP -> {
                    viewPager.isUserInputEnabled = true
                }
            }

            false
        }
    }


// ---------------- MAP ----------------

    private fun setupMap() {

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true

        val khamgaon = LatLng(20.706344, 76.572811)

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(khamgaon, 14f)
        )

        loadNearbyPlaces()

        googleMap.setOnCameraMoveStartedListener {

            requireActivity()
                .findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
                .isUserInputEnabled = false
        }

        googleMap.setOnCameraIdleListener {

            requireActivity()
                .findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
                .isUserInputEnabled = true
        }
        googleMap.setOnMapClickListener {

            val intent = Intent(requireContext(), FullMapActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loadNearbyPlaces() {

        db.collection("services")
            .get()
            .addOnSuccessListener { documents ->

                for (doc in documents) {

                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")

                    if (lat == null || lng == null) continue

                    val name = doc.getString("serviceName") ?: ""

                    val location = LatLng(lat, lng)

                    mMap?.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(name)
                    )
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

                    val serviceId = doc.id

                    val ownerId = doc.getString("ownerId") ?: ""

                    val photosRaw = doc.get("photos") as? List<*> ?: emptyList<Any>()
                    val photos = photosRaw.filterIsInstance<String>()

                    val serviceName = doc.getString("serviceName") ?: ""

                    val latitude = doc.getDouble("latitude") ?: 0.0
                    val longitude = doc.getDouble("longitude") ?: 0.0
                    val phone = doc.get("contact")?.toString() ?: ""
                    val description = doc.getString("description") ?: ""

                    servicesList.add(
                        Service(
                            serviceId,
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

                if (!isAdded) return@addOnSuccessListener

                binding.servicesRecycler.adapter =
                    ServicesAdapter(servicesList)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to load services", it)
            }
    }

// ---------------- DRAWER --------------



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
