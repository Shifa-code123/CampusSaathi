package com.example.campussaathi

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerAddNewList4 : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var mMap: GoogleMap

    private var listingId: String? = null
    private var ownerType: String? = null

    // 📍 MAP DATA
    private var selectedLatLng: LatLng? = null

    // 👉 CHANGE this to your REAL campus location
    private val campusLatLng = LatLng(18.5204, 73.8567) // Pune example

    // Address
    private lateinit var etArea: EditText
    private lateinit var etLandmark: EditText
    private lateinit var etCity: EditText
    private lateinit var etPincode: EditText
    private lateinit var txtDistance: TextView

    // Room
    private lateinit var cbNearCampus: CheckBox
    private lateinit var cbMarketNearby: CheckBox
    private lateinit var cbHospitalNearby: CheckBox

    // Mess
    private lateinit var etServiceRadius: EditText
    private lateinit var cbHomeDelivery: CheckBox
    private lateinit var cbPickupAvailable: CheckBox

    // Tuition
    private lateinit var cbMainRoad: CheckBox
    private lateinit var cbSafeArea: CheckBox
    private lateinit var cbParkingAvailable: CheckBox

    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list4)

        db = FirebaseFirestore.getInstance()

        listingId = intent.getStringExtra("LISTING_ID")
        ownerType = intent.getStringExtra("OWNER_TYPE")

        initViews()

        // 🔥 MAP INIT
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnNext.setOnClickListener {
            saveStep4()
        }
    }

    private fun initViews() {
        etArea = findViewById(R.id.etArea)
        etLandmark = findViewById(R.id.etLandmark)
        etCity = findViewById(R.id.etCity)
        etPincode = findViewById(R.id.etPincode)
        txtDistance = findViewById(R.id.txtDistance)

        cbNearCampus = findViewById(R.id.cbNearCampus)
        cbMarketNearby = findViewById(R.id.cbMarketNearby)
        cbHospitalNearby = findViewById(R.id.cbHospitalNearby)

        etServiceRadius = findViewById(R.id.etServiceRadius)
        cbHomeDelivery = findViewById(R.id.cbHomeDelivery)
        cbPickupAvailable = findViewById(R.id.cbPickupAvailable)

        cbMainRoad = findViewById(R.id.cbMainRoad)
        cbSafeArea = findViewById(R.id.cbSafeArea)
        cbParkingAvailable = findViewById(R.id.cbParkingAvailable)

        btnNext = findViewById(R.id.btnNextStep4)
    }

    /* ================= MAP ================= */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(campusLatLng, 14f)
        )

        // Campus marker
        mMap.addMarker(
            MarkerOptions().position(campusLatLng).title("Campus")
        )

        mMap.setOnMapClickListener(this)
    }

    override fun onMapClick(latLng: LatLng) {
        mMap.clear()

        mMap.addMarker(
            MarkerOptions().position(campusLatLng).title("Campus")
        )

        mMap.addMarker(
            MarkerOptions().position(latLng).title("Selected Location")
        )

        selectedLatLng = latLng
        calculateDistance(latLng)
    }

    private fun calculateDistance(dest: LatLng) {
        val result = FloatArray(1)

        Location.distanceBetween(
            campusLatLng.latitude,
            campusLatLng.longitude,
            dest.latitude,
            dest.longitude,
            result
        )

        val km = result[0] / 1000
        txtDistance.text = "Distance from campus: %.2f km".format(km)
    }

    /* ================= SAVE ================= */

    private fun saveStep4() {

        if (listingId == null) {
            toast("Listing ID missing")
            return
        }

        if (selectedLatLng == null) {
            toast("Please select location on map")
            return
        }

        if (etArea.text.isEmpty() || etCity.text.isEmpty() || etPincode.text.isEmpty()) {
            toast("Please fill area, city and pincode")
            return
        }

        val data = hashMapOf<String, Any>()

        /* ---------- COMMON ADDRESS ---------- */
        data["area"] = etArea.text.toString()
        data["landmark"] = etLandmark.text.toString()
        data["city"] = etCity.text.toString()
        data["pincode"] = etPincode.text.toString()

        data["latitude"] = selectedLatLng!!.latitude
        data["longitude"] = selectedLatLng!!.longitude
        data["distanceFromCampus"] = txtDistance.text.toString()

        /* ---------- OWNER TYPE BASED ---------- */
        when (ownerType) {

            "ROOM" -> {
                data["nearCampus"] = cbNearCampus.isChecked
                data["marketNearby"] = cbMarketNearby.isChecked
                data["hospitalNearby"] = cbHospitalNearby.isChecked
            }

            "MESS" -> {
                data["serviceRadius"] = etServiceRadius.text.toString()
                data["homeDelivery"] = cbHomeDelivery.isChecked
                data["pickupAvailable"] = cbPickupAvailable.isChecked
            }

            "TUITION" -> {
                data["nearMainRoad"] = cbMainRoad.isChecked
                data["safeArea"] = cbSafeArea.isChecked
                data["parkingAvailable"] = cbParkingAvailable.isChecked
            }
        }

        db.collection("listings")
            .document(listingId!!)
            .update(data)
            .addOnSuccessListener {
                toast("Step-4 saved")

                val i = Intent(this, ActivityOwnerAddNewList5::class.java)
                i.putExtra("LISTING_ID", listingId)
                i.putExtra("OWNER_TYPE", ownerType)
                startActivity(i)
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
