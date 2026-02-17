package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerAddNewList2 : AppCompatActivity() {

    // Layouts
    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    // Room fields
    private lateinit var etRoomRent: EditText
    private lateinit var etRoomDeposit: EditText
    private lateinit var rgRentType: RadioGroup
    private lateinit var rgAvailability: RadioGroup
    private lateinit var etTotalUnits: EditText
    private lateinit var etAvailableUnits: EditText
    private lateinit var etAvailableFrom: EditText

    // Mess fields
    private lateinit var etMessMonthly: EditText
    private lateinit var etMessDaily: EditText

    // Tuition fields
    private lateinit var etTuitionFees: EditText
    private lateinit var etCourseDuration: EditText

    private lateinit var btnNext: Button
    private lateinit var db: FirebaseFirestore

    private var ownerType: String? = null
    private var listingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list2)

        db = FirebaseFirestore.getInstance()

        ownerType = intent.getStringExtra("OWNER_TYPE")
        listingId = intent.getStringExtra("LISTING_ID")

        if (ownerType == null || listingId == null) {
            toast("Invalid owner or listing")
            finish()
            return
        }

        initViews()
        showCorrectLayout()
        availabilityLogic()

        btnNext.setOnClickListener {
            saveStep2()
        }
    }

    private fun initViews() {
        layoutRoom = findViewById(R.id.layoutRoomStep2)
        layoutMess = findViewById(R.id.layoutMessStep2)
        layoutTuition = findViewById(R.id.layoutTuitionStep2)

        etRoomRent = findViewById(R.id.etRoomRent)
        etRoomDeposit = findViewById(R.id.etRoomDeposit)
        rgRentType = findViewById(R.id.rgRentType)
        rgAvailability = findViewById(R.id.rgAvailability)
        etTotalUnits = findViewById(R.id.etTotalUnits)
        etAvailableUnits = findViewById(R.id.etAvailableUnits)
        etAvailableFrom = findViewById(R.id.etAvailableFrom)

        etMessMonthly = findViewById(R.id.etMessMonthlyCharge)
        etMessDaily = findViewById(R.id.etMessDailyCharge)

        etTuitionFees = findViewById(R.id.etTuitionFees)
        etCourseDuration = findViewById(R.id.etCourseDuration)

        btnNext = findViewById(R.id.btnNextStep2)
    }

    private fun showCorrectLayout() {

        layoutRoom.visibility = View.GONE
        layoutMess.visibility = View.GONE
        layoutTuition.visibility = View.GONE

        when (ownerType?.lowercase()) {

            "room", "room_pg" -> layoutRoom.visibility = View.VISIBLE

            "mess" -> layoutMess.visibility = View.VISIBLE

            "tuition" -> layoutTuition.visibility = View.VISIBLE

            else -> toast("Invalid owner type: $ownerType")
        }
    }

    private fun availabilityLogic() {

        rgAvailability.setOnCheckedChangeListener { _, checkedId ->

            val selected =
                findViewById<RadioButton>(checkedId).text.toString()

            if (selected == "Vacant") {
                etTotalUnits.visibility = View.VISIBLE
                etAvailableUnits.visibility = View.VISIBLE
            } else {
                etAvailableUnits.setText("0")
                etTotalUnits.visibility = View.GONE
                etAvailableUnits.visibility = View.GONE
            }
        }
    }

    private fun saveStep2() {

        val data = hashMapOf<String, Any>()

        when (ownerType?.lowercase()) {

            // 🔵 ROOM
            "room", "room_pg" -> {

                if (etRoomRent.text.isEmpty()
                    || rgRentType.checkedRadioButtonId == -1
                    || rgAvailability.checkedRadioButtonId == -1
                ) {
                    toast("Fill all required room details")
                    return
                }

                val availability =
                    findViewById<RadioButton>(rgAvailability.checkedRadioButtonId)
                        .text.toString()

                data["rent"] = etRoomRent.text.toString()
                data["deposit"] = etRoomDeposit.text.toString()
                data["rentType"] =
                    findViewById<RadioButton>(rgRentType.checkedRadioButtonId)
                        .text.toString()
                data["availability"] = availability
                data["totalUnits"] = etTotalUnits.text.toString().ifEmpty { "0" }
                data["availableUnits"] = etAvailableUnits.text.toString().ifEmpty { "0" }
                data["availableFrom"] = etAvailableFrom.text.toString()
            }

            // 🟢 MESS
            "mess" -> {

                if (etMessMonthly.text.isEmpty()) {
                    toast("Enter monthly charges")
                    return
                }

                data["monthlyCharge"] = etMessMonthly.text.toString()
                data["dailyCharge"] = etMessDaily.text.toString()
            }

            // 🟣 TUITION
            "tuition" -> {

                if (etTuitionFees.text.isEmpty()) {
                    toast("Enter tuition fees")
                    return
                }

                data["fees"] = etTuitionFees.text.toString()
                data["duration"] = etCourseDuration.text.toString()
            }
        }

        // 🔥 SAME DOCUMENT UPDATE
        db.collection("listings")
            .document(listingId!!)
            .update(data)
            .addOnSuccessListener {

                toast("Step 2 saved")

                val i = Intent(this, ActivityOwnerAddNewList3::class.java)
                i.putExtra("LISTING_ID", listingId)
                i.putExtra("OWNER_TYPE", ownerType)
                startActivity(i)
                finish()
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
