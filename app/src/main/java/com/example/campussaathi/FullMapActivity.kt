package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore

class FullMapActivity : AppCompatActivity(), OnMapReadyCallback {


    private var mMap: GoogleMap? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_map)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.fullMap) as SupportMapFragment

        mapFragment.getMapAsync(this)
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
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED
                                )
                            )
                    )
                }
            }
    }


}
