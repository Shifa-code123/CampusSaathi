package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class FullMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private val db = FirebaseFirestore.getInstance()
    private val markerMap = HashMap<String, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fullMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        val khamgaon = LatLng(20.706344, 76.572811)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(khamgaon, 14f))

        listenToNearbyPlaces()

        googleMap.setOnInfoWindowClickListener { marker ->
            val service = marker.tag as? Service
            service?.let {
                val intent = Intent(this, StudentActivity::class.java).apply {
                    putExtra("targetFragment", "ServiceDetail")
                    putExtra("serviceId", it.serviceId)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
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
}