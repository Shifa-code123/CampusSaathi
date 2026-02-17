package com.example.campussaathi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ActivityOwnerAddNewList4 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: MapView
    private lateinit var txtDistance: TextView
    private lateinit var btnGps: ImageView
    private lateinit var btnNext: Button

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    private var ownerType: String? = null
    private var listingId: String? = null

    private var selectedGeoPoint: GeoPoint? = null
    private var distanceKm: Double = 0.0

    private val LOCATION_PERMISSION_CODE = 100

    private val campusLat = 20.7074
    private val campusLng = 76.5680

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list4)

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ownerType = intent.getStringExtra("OWNER_TYPE")?.lowercase()
        listingId = intent.getStringExtra("LISTING_ID")


        initViews()
        showCorrectLayout()
        setupMap()

        btnGps.setOnClickListener {
            checkLocationPermission()
        }

        btnNext.setOnClickListener {
            saveStep4()
        }
    }

    private fun initViews() {
        map = findViewById(R.id.map)
        txtDistance = findViewById(R.id.txtDistance)
        btnGps = findViewById(R.id.btnGps)
        btnNext = findViewById(R.id.btnNextStep4)

        layoutRoom = findViewById(R.id.layoutRoomStep4)
        layoutMess = findViewById(R.id.layoutMessStep4)
        layoutTuition = findViewById(R.id.layoutTuitionStep4)
    }

    private fun showCorrectLayout() {
        layoutRoom.visibility = View.GONE
        layoutMess.visibility = View.GONE
        layoutTuition.visibility = View.GONE

        when (ownerType) {
            "room", "room_pg" -> layoutRoom.visibility = View.VISIBLE
            "mess" -> layoutMess.visibility = View.VISIBLE
            "tuition" -> layoutTuition.visibility = View.VISIBLE
        }
    }

    /* ================= MAP ================= */

    private fun setupMap() {

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osm", MODE_PRIVATE)
        )

        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val startPoint = GeoPoint(campusLat, campusLng)
        map.controller.setZoom(16.0)
        map.controller.setCenter(startPoint)

        map.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                val projection = map.projection
                val geoPoint = projection.fromPixels(
                    event.x.toInt(),
                    event.y.toInt()
                ) as GeoPoint

                selectedGeoPoint = geoPoint
                showMarker(geoPoint)
                calculateDistance(geoPoint)
            }
            false
        }
    }

    private fun showMarker(point: GeoPoint) {
        map.overlays.clear()

        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map.overlays.add(marker)
        map.invalidate()
    }

    /* ================= GPS ================= */

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->

            if (location != null) {

                val geoPoint = GeoPoint(location.latitude, location.longitude)
                selectedGeoPoint = geoPoint

                map.controller.setCenter(geoPoint)
                showMarker(geoPoint)
                calculateDistance(geoPoint)

            } else {
                Toast.makeText(this, "Turn ON GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* ================= DISTANCE ================= */

    private fun calculateDistance(dest: GeoPoint) {

        val result = FloatArray(1)

        Location.distanceBetween(
            campusLat,
            campusLng,
            dest.latitude,
            dest.longitude,
            result
        )

        distanceKm = result[0] / 1000.0

        txtDistance.text = String.format(
            "Distance from campus: %.2f km",
            distanceKm
        )
    }

    /* ================= SAVE ================= */

    private fun saveStep4() {

        if (listingId == null) {
            Toast.makeText(this, "Listing ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedGeoPoint == null) {
            Toast.makeText(this, "Select location on map", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "ID = $listingId", Toast.LENGTH_LONG).show()


        val data = hashMapOf(
            "latitude" to selectedGeoPoint!!.latitude,
            "longitude" to selectedGeoPoint!!.longitude,
            "distanceFromCampus" to distanceKm
        )

        db.collection("listings")
            .document(listingId!!)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(this, "Step 4 Saved", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, TestActivity::class.java)
                intent.putExtra("LISTING_ID", listingId)
                intent.putExtra("OWNER_TYPE", ownerType)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

}
