package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerAddNewList3 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var listingId: String? = null
    private var ownerType: String? = null

    // Layout groups
    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list3)

        db = FirebaseFirestore.getInstance()

        listingId = intent.getStringExtra("LISTING_ID")
        ownerType = intent.getStringExtra("OWNER_TYPE")?.lowercase()

        if (listingId == null) {
            toast("Listing ID missing")
            finish()
            return
        }

        // 🔥 layout ids (make sure XML me same ids hai)
        layoutRoom = findViewById(R.id.layoutRoomStep3)
        layoutMess = findViewById(R.id.layoutMessStep3)
        layoutTuition = findViewById(R.id.layoutTuitionStep3)

        showCorrectLayout()

        findViewById<Button>(R.id.btnNextStep3).setOnClickListener {
            saveStep3()
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
            else -> toast("Invalid owner type")
        }
    }

    private fun saveStep3() {

        val amenities = mutableListOf<String>()

        when (ownerType) {

            // ================= ROOM =================
            "room", "room_pg" -> {

                addCB(R.id.cbWifi, "Wi-Fi", amenities)
                addCB(R.id.cbWater, "Water Supply", amenities)
                addCB(R.id.cbElectricity, "Electricity Included", amenities)
                addCB(R.id.cbFanLight, "Fan and Light", amenities)
                addCB(R.id.cbBed, "Bed and Mattress", amenities)
                addCB(R.id.cbStudyTable, "Study Table", amenities)
                addCB(R.id.cbCupboard, "Cupboard / Wardrobe", amenities)
                addCB(R.id.cbHotWater, "Hot Water", amenities)
                addCB(R.id.cbCleaning, "Daily Cleaning", amenities)
                addCB(R.id.cbToiletType, "Western / Indian Toilet", amenities)
                addCB(R.id.cbVisitors, "Visitors Allowed", amenities)
                addCB(R.id.cbOutsideFood, "Outside Food Allowed", amenities)

                val bathGroup = findViewById<RadioGroup>(R.id.rgBathroomType)
                if (bathGroup.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(bathGroup.checkedRadioButtonId)
                            .text.toString()
                    )
                }
            }

            // ================= MESS =================
            "mess" -> {

                addCB(R.id.cbBreakfast, "Breakfast", amenities)
                addCB(R.id.cbLunch, "Lunch", amenities)
                addCB(R.id.cbDinner, "Dinner", amenities)
                addCB(R.id.cbFestivalMeal, "Festival Special Meals", amenities)
                addCB(R.id.cbTiffin, "Tiffin Service", amenities)
                addCB(R.id.cbDelivery, "Home Delivery", amenities)
                addCB(R.id.cbTrial, "Trial Available", amenities)

                val messType = findViewById<RadioGroup>(R.id.rgMessType)
                if (messType.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(messType.checkedRadioButtonId)
                            .text.toString()
                    )
                }
            }

            // ================= TUITION =================
            "tuition" -> {

                addCB(R.id.cbBoard, "Whiteboard / Digital Board", amenities)
                addCB(R.id.cbNotes, "Printed Notes", amenities)
                addCB(R.id.cbRecorded, "Recorded Lectures", amenities)
                addCB(R.id.cbDoubt, "Doubt Solving Sessions", amenities)
                addCB(R.id.cbTestSeries, "Test Series", amenities)

                val tuitionMode = findViewById<RadioGroup>(R.id.rgTuitionMode)
                if (tuitionMode.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(tuitionMode.checkedRadioButtonId)
                            .text.toString()
                    )
                }
            }
        }

        db.collection("listings")
            .document(listingId!!)
            .update("amenities", amenities)
            .addOnSuccessListener {

                toast("Step 3 saved successfully")

                val i = Intent(this, ActivityOwnerAddNewList4::class.java)
                i.putExtra("LISTING_ID", listingId)
                i.putExtra("OWNER_TYPE", ownerType)
                startActivity(i)
                finish()
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun addCB(id: Int, value: String, list: MutableList<String>) {
        val cb = findViewById<CheckBox>(id)
        if (cb.isChecked) list.add(value)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
