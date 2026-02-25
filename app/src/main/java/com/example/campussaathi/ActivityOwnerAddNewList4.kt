package com.example.campussaathi

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.location.FusedLocationProviderClient


class ActivityOwnerAddNewList4 : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {

    private lateinit var db: FirebaseFirestore
    private var mMap: GoogleMap? = null

    private var selectedLatLng: LatLng? = null
    private var distanceKm: Double = 0.0
    private var ownerType: String? = null

    private val campusLatLng = LatLng(20.7074, 76.5680)

    private lateinit var etArea: EditText
    private lateinit var btnGps: ImageView
    private lateinit var etLandmark: EditText
    private lateinit var etCity: EditText
    private lateinit var etPincode: EditText
    private lateinit var txtDistance: TextView
    private lateinit var btnNext: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 100

    // Layouts
    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

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

    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private val uid by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list4)

        if (uid == null) {
            toast("User not logged in")
            finish()
            return
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        db = FirebaseFirestore.getInstance()

        setupDrawer()
        initViews()
        initMap()
        fetchOwnerType()

        btnNext.setOnClickListener {
            saveStep4()
        }
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun initViews() {
        btnGps = findViewById(R.id.btnGps)

        btnGps.setOnClickListener {
            getCurrentLocation()
        }

        etArea = findViewById(R.id.etArea)
        etLandmark = findViewById(R.id.etLandmark)
        etCity = findViewById(R.id.etCity)
        etPincode = findViewById(R.id.etPincode)
        txtDistance = findViewById(R.id.txtDistance)
        btnNext = findViewById(R.id.btnNextStep4)

        layoutRoom = findViewById(R.id.layoutRoomStep4)
        layoutMess = findViewById(R.id.layoutMessStep4)
        layoutTuition = findViewById(R.id.layoutTuitionStep4)


        cbNearCampus = findViewById(R.id.cbNearCampus)
        cbMarketNearby = findViewById(R.id.cbMarketNearby)
        cbHospitalNearby = findViewById(R.id.cbHospitalNearby)

        etServiceRadius = findViewById(R.id.etServiceRadius)
        cbHomeDelivery = findViewById(R.id.cbHomeDelivery)
        cbPickupAvailable = findViewById(R.id.cbPickupAvailable)

        cbMainRoad = findViewById(R.id.cbMainRoad)
        cbSafeArea = findViewById(R.id.cbSafeArea)
        cbParkingAvailable = findViewById(R.id.cbParkingAvailable)
    }

    private fun fetchOwnerType() {

        db.collection("users")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")?.lowercase()

                showCorrectLayout()
            }
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

    private fun initMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLatLng, 16f))
        mMap?.addMarker(MarkerOptions().position(campusLatLng).title("Campus"))
        mMap?.setOnMapClickListener(this)
    }

    override fun onMapClick(latLng: LatLng) {

        mMap?.clear()
        mMap?.addMarker(MarkerOptions().position(campusLatLng).title("Campus"))
        mMap?.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

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

        distanceKm = result[0] / 1000.0
        txtDistance.text = "Distance from campus: %.2f km".format(distanceKm)
    }

    private fun saveStep4() {

        if (selectedLatLng == null) {
            toast("Select location on map")
            return
        }

        if (etArea.text.isEmpty() ||
            etCity.text.isEmpty() ||
            etPincode.text.isEmpty()
        ) {
            toast("Fill required fields")
            return
        }

        val data = hashMapOf<String, Any>(
            "area" to etArea.text.toString(),
            "landmark" to etLandmark.text.toString(),
            "city" to etCity.text.toString(),
            "pincode" to etPincode.text.toString(),
            "latitude" to selectedLatLng!!.latitude,
            "longitude" to selectedLatLng!!.longitude,
            "distanceFromCampus" to distanceKm,
            "currentStep" to 5
        )

        when (ownerType) {
            "room", "room_pg" -> {
                data["nearCampus"] = cbNearCampus.isChecked
                data["marketNearby"] = cbMarketNearby.isChecked
                data["hospitalNearby"] = cbHospitalNearby.isChecked
            }

            "mess" -> {
                data["serviceRadius"] = etServiceRadius.text.toString()
                data["homeDelivery"] = cbHomeDelivery.isChecked
                data["pickupAvailable"] = cbPickupAvailable.isChecked
            }

            "tuition" -> {
                data["nearMainRoad"] = cbMainRoad.isChecked
                data["safeArea"] = cbSafeArea.isChecked
                data["parkingAvailable"] = cbParkingAvailable.isChecked
            }
        }

        db.collection("listings")
            .document(uid!!)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {

                toast("Step 4 saved")

                val intent = Intent(this, ActivityOwnerAddNewList5::class.java)
                intent.putExtra("LISTING_ID", uid) // or your existing document ID
                startActivity(intent)

                finish()
            }
    }
    private fun getCurrentLocation() {

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val latLng = LatLng(location.latitude, location.longitude)

                    selectedLatLng = latLng

                    mMap?.clear()

                    mMap?.addMarker(
                        MarkerOptions()
                            .position(campusLatLng)
                            .title("Campus")
                    )

                    mMap?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Your Location")
                    )

                    mMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                    )

                    calculateDistance(latLng)

                    toast("Current location detected")

                } else {
                    toast("Unable to get location")
                }
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}