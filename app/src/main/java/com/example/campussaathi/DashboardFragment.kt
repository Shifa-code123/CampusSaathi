package com.example.campussaathi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var mMap: GoogleMap? = null
    private val markerMap = HashMap<String, Marker>()

    private var selectedCity: String? = null
    private var selectedCollege: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("CampusSaathiPrefs", Context.MODE_PRIVATE)
        selectedCity = prefs.getString("selected_city", null)
        selectedCollege = prefs.getString("selected_college", null)

        binding.btnMenu.setOnClickListener {
            (activity as? StudentActivity)?.openDrawer()
        }

        binding.servicesRecycler.layoutManager = LinearLayoutManager(requireContext())

        loadServices()
        setupMap()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))

        val khamgaon = LatLng(20.706344, 76.572811)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(khamgaon, 14f))

        listenToNearbyPlaces()

        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        googleMap.setOnCameraMoveStartedListener { viewPager?.isUserInputEnabled = false }
        googleMap.setOnCameraIdleListener { viewPager?.isUserInputEnabled = true }

        googleMap.setOnMapClickListener {
            startActivity(Intent(requireContext(), FullMapActivity::class.java))
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            val service = marker.tag as? Service
            service?.let {
                val fragment = Student_ServiceDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("serviceId", it.serviceId)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun getMarkerColor(category: String?): Float {
        return when (category) {
            "Mess" -> BitmapDescriptorFactory.HUE_ORANGE
            "Room" -> BitmapDescriptorFactory.HUE_BLUE
            "Tuition" -> BitmapDescriptorFactory.HUE_YELLOW
            "Street Food" -> BitmapDescriptorFactory.HUE_RED
            "Medical Stores", "Medical" -> BitmapDescriptorFactory.HUE_GREEN
            "Stationary Stores", "Stationery" -> BitmapDescriptorFactory.HUE_CYAN
            "Gym", "Fitness" -> BitmapDescriptorFactory.HUE_VIOLET
            "College Services", "Others" -> BitmapDescriptorFactory.HUE_ROSE
            else -> BitmapDescriptorFactory.HUE_RED
        }
    }

    private fun listenToNearbyPlaces() {
        // Only load data if city == "Khamgaon" AND college == "GPK"
        if (selectedCity != "Khamgaon" || selectedCollege != "GPK") {
            return
        }

        db.collection("services").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            for (dc in snapshots.documentChanges) {
                val doc = dc.document
                val serviceId = doc.id
                when (dc.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val lat = doc.getDouble("latitude")
                        val lng = doc.getDouble("longitude")
                        if (lat == null || lng == null) continue

                        val name = doc.getString("serviceName") ?: ""
                        val category = doc.getString("category") ?: ""
                        val location = LatLng(lat, lng)

                        markerMap[serviceId]?.remove()

                        val service = Service(
                            serviceId,
                            doc.getString("ownerId") ?: "",
                            doc.get("photos") as? List<String> ?: emptyList(),
                            name,
                            lat,
                            lng,
                            doc.get("contact")?.toString() ?: "",
                            doc.getString("description") ?: "",
                            category
                        )

                        val marker = mMap?.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(name)
                                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(category)))
                        )
                        marker?.tag = service
                        if (marker != null) markerMap[serviceId] = marker
                    }
                    DocumentChange.Type.REMOVED -> {
                        markerMap[serviceId]?.remove()
                        markerMap.remove(serviceId)
                    }
                }
            }
        }
    }

    private fun loadServices() {
        // Only load data if city == "Khamgaon" AND college == "GPK"
        if (selectedCity != "Khamgaon" || selectedCollege != "GPK") {
            binding.servicesRecycler.adapter = ServicesAdapter(emptyList())
            return
        }

        db.collection("services").get().addOnSuccessListener { docs ->
            val servicesList = ArrayList<Service>()
            for (doc in docs) {
                servicesList.add(Service(
                    doc.id,
                    doc.getString("ownerId") ?: "",
                    doc.get("photos") as? List<String> ?: emptyList(),
                    doc.getString("serviceName") ?: "",
                    doc.getDouble("latitude") ?: 0.0,
                    doc.getDouble("longitude") ?: 0.0,
                    doc.get("contact")?.toString() ?: "",
                    doc.getString("description") ?: "",
                    doc.getString("category") ?: ""
                ))
            }
            if (!isAdded) return@addOnSuccessListener
            binding.servicesRecycler.adapter = ServicesAdapter(servicesList)
        }.addOnFailureListener { Log.e("Firestore", "Failed", it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
