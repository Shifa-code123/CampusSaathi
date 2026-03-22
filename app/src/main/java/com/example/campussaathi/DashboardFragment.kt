package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import android.view.MotionEvent
import android.widget.ImageView
import com.example.campussaathi.utils.DrawerManager
import com.example.campussaathi.utils.ProfileImageLoader

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var mMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Drawer setup (ONLY ONCE)
        DrawerManager.setupDrawer(
            requireActivity(),
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.drawerLayout.setDrawerLockMode(
            androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
        )

        // ✅ Drawer image
        val drawerImage = binding.studentDrawer.root
            .findViewById<ImageView>(R.id.profileImage)

        ProfileImageLoader.loadProfile(drawerImage)

        // ✅ Menu click
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Recycler
        binding.servicesRecycler.layoutManager =
            LinearLayoutManager(requireContext())

        loadServices()
        setupMap()
    }

    private fun setupMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true

        val khamgaon = LatLng(20.706344, 76.572811)

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(khamgaon, 14f)
        )

        loadNearbyPlaces()

        val viewPager =
            requireActivity().findViewById<ViewPager2>(R.id.viewPager)

        // ✔ ONLY THIS CONTROL (CORRECT WAY)
        googleMap.setOnCameraMoveStartedListener {
            viewPager.isUserInputEnabled = false
        }

        googleMap.setOnCameraIdleListener {
            viewPager.isUserInputEnabled = true
        }

        googleMap.setOnMapClickListener {
            startActivity(Intent(requireContext(), FullMapActivity::class.java))
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
                        MarkerOptions().position(location).title(name)
                    )
                }
            }
    }

    private fun loadServices() {
        db.collection("services")
            .get()
            .addOnSuccessListener { docs ->

                val servicesList = ArrayList<Service>()

                for (doc in docs) {

                    servicesList.add(
                        Service(
                            doc.id,
                            doc.getString("ownerId") ?: "",
                            doc.get("photos") as? List<String> ?: emptyList(),
                            doc.getString("serviceName") ?: "",
                            doc.getDouble("latitude") ?: 0.0,
                            doc.getDouble("longitude") ?: 0.0,
                            doc.get("contact")?.toString() ?: "",
                            doc.getString("description") ?: ""
                        )
                    )
                }

                if (!isAdded) return@addOnSuccessListener

                binding.servicesRecycler.adapter =
                    ServicesAdapter(servicesList)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed", it)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}