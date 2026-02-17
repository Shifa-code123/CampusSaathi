package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerEditListing : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null
    private var ownerType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_edit_listing)

        uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        loadData()
    }

    private fun loadData() {

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")

                val layoutRoom = findViewById<LinearLayout>(R.id.layoutRoom)
                val layoutMess = findViewById<LinearLayout>(R.id.layoutMess)
                val layoutTuition = findViewById<LinearLayout>(R.id.layoutTuition)

                layoutRoom.visibility = View.GONE
                layoutMess.visibility = View.GONE
                layoutTuition.visibility = View.GONE

                when (ownerType) {

                    "room_pg" -> {
                        layoutRoom.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etPropertyName)
                            .setText(doc.getString("propertyName"))

                        findViewById<EditText>(R.id.etRent)
                            .setText(doc.getString("rent"))

                        findViewById<EditText>(R.id.etTotalUnits)
                            .setText(doc.getString("totalUnits"))

                        findViewById<EditText>(R.id.etAvailableUnits)
                            .setText(doc.getString("availableUnits"))
                    }

                    "mess" -> {
                        layoutMess.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etMessName)
                            .setText(doc.getString("messName"))

                        findViewById<EditText>(R.id.etMonthlyCharge)
                            .setText(doc.getString("monthlyCharge"))

                        findViewById<EditText>(R.id.etDailyCharge)
                            .setText(doc.getString("dailyCharge"))
                    }

                    "tuition" -> {
                        layoutTuition.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etTuitionName)
                            .setText(doc.getString("tuitionName"))

                        findViewById<EditText>(R.id.etFees)
                            .setText(doc.getString("fees"))

                        findViewById<EditText>(R.id.etDuration)
                            .setText(doc.getString("duration"))
                    }
                }

                findViewById<EditText>(R.id.etArea)
                    .setText(doc.getString("area"))

                findViewById<EditText>(R.id.etCity)
                    .setText(doc.getString("city"))

                findViewById<EditText>(R.id.etLandmark)
                    .setText(doc.getString("landmark"))
            }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {

        val updates = hashMapOf<String, Any>()

        when (ownerType) {

            "room_pg" -> {
                updates["propertyName"] =
                    findViewById<EditText>(R.id.etPropertyName).text.toString()

                updates["rent"] =
                    findViewById<EditText>(R.id.etRent).text.toString()

                updates["totalUnits"] =
                    findViewById<EditText>(R.id.etTotalUnits).text.toString()

                updates["availableUnits"] =
                    findViewById<EditText>(R.id.etAvailableUnits).text.toString()
            }

            "mess" -> {
                updates["messName"] =
                    findViewById<EditText>(R.id.etMessName).text.toString()

                updates["monthlyCharge"] =
                    findViewById<EditText>(R.id.etMonthlyCharge).text.toString()

                updates["dailyCharge"] =
                    findViewById<EditText>(R.id.etDailyCharge).text.toString()
            }

            "tuition" -> {
                updates["tuitionName"] =
                    findViewById<EditText>(R.id.etTuitionName).text.toString()

                updates["fees"] =
                    findViewById<EditText>(R.id.etFees).text.toString()

                updates["duration"] =
                    findViewById<EditText>(R.id.etDuration).text.toString()
            }
        }

        updates["area"] =
            findViewById<EditText>(R.id.etArea).text.toString()

        updates["city"] =
            findViewById<EditText>(R.id.etCity).text.toString()

        updates["landmark"] =
            findViewById<EditText>(R.id.etLandmark).text.toString()

        db.collection("listings")
            .document(uid!!)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
