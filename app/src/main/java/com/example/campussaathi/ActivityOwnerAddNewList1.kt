package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerAddNewList1 : AppCompatActivity() {

    private lateinit var roomLayout: LinearLayout
    private lateinit var messLayout: LinearLayout
    private lateinit var tuitionLayout: LinearLayout

    // ROOM
    private lateinit var etPropertyName: EditText
    private lateinit var rgPropertyType: RadioGroup
    private lateinit var rgGenderAllowed: RadioGroup

    // MESS
    private lateinit var etMessName: EditText
    private lateinit var rgMessType: RadioGroup

    // TUITION
    private lateinit var etTuitionName: EditText
    private lateinit var rgTuitionType: RadioGroup

    private lateinit var btnNext: Button

    private lateinit var db: FirebaseFirestore
    private var ownerType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list1)

        db = FirebaseFirestore.getInstance()

        initViews()
        fetchOwnerType()

        btnNext.setOnClickListener {
            saveStep1()
        }
    }

    private fun initViews() {
        roomLayout = findViewById(R.id.layoutRoomStep1)
        messLayout = findViewById(R.id.layoutMessStep1)
        tuitionLayout = findViewById(R.id.layoutTuitionStep1)

        etPropertyName = findViewById(R.id.etPropertyName)
        rgPropertyType = findViewById(R.id.rgPropertyType)
        rgGenderAllowed = findViewById(R.id.rgGenderAllowed)

        etMessName = findViewById(R.id.etMessName)
        rgMessType = findViewById(R.id.rgMessType)

        etTuitionName = findViewById(R.id.etTuitionName)
        rgTuitionType = findViewById(R.id.rgTuitionType)

        btnNext = findViewById(R.id.btnNext)
    }

    // 🔥 Fetch owner type from users collection
    private fun fetchOwnerType() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            toast("User not logged in")
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")?.lowercase()

                if (ownerType == null) {
                    toast("Owner type missing in users collection")
                    return@addOnSuccessListener
                }

                showCorrectLayout()
            }
            .addOnFailureListener {
                toast("Failed to load owner type")
            }
    }

    private fun showCorrectLayout() {

        roomLayout.visibility = View.GONE
        messLayout.visibility = View.GONE
        tuitionLayout.visibility = View.GONE

        when (ownerType) {

            "room_pg", "room" -> roomLayout.visibility = View.VISIBLE

            "mess" -> messLayout.visibility = View.VISIBLE

            "tuition" -> tuitionLayout.visibility = View.VISIBLE

            else -> toast("Invalid owner type: $ownerType")
        }
    }

    private fun saveStep1() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            toast("User not logged in")
            return
        }

        val data = hashMapOf<String, Any>()

        when (ownerType) {

            "room_pg", "room" -> {

                if (etPropertyName.text.isEmpty()
                    || rgPropertyType.checkedRadioButtonId == -1
                    || rgGenderAllowed.checkedRadioButtonId == -1) {

                    toast("Fill all Room details")
                    return
                }

                data["ownerType"] = "room_pg"
                data["propertyName"] = etPropertyName.text.toString()
                data["propertyType"] =
                    findViewById<RadioButton>(rgPropertyType.checkedRadioButtonId).text.toString()
                data["genderAllowed"] =
                    findViewById<RadioButton>(rgGenderAllowed.checkedRadioButtonId).text.toString()
            }

            "mess" -> {

                if (etMessName.text.isEmpty()
                    || rgMessType.checkedRadioButtonId == -1) {

                    toast("Fill all Mess details")
                    return
                }

                data["ownerType"] = "mess"
                data["messName"] = etMessName.text.toString()
                data["messType"] =
                    findViewById<RadioButton>(rgMessType.checkedRadioButtonId).text.toString()
            }

            "tuition" -> {

                if (etTuitionName.text.isEmpty()
                    || rgTuitionType.checkedRadioButtonId == -1) {

                    toast("Fill all Tuition details")
                    return
                }

                data["ownerType"] = "tuition"
                data["tuitionName"] = etTuitionName.text.toString()
                data["tuitionType"] =
                    findViewById<RadioButton>(rgTuitionType.checkedRadioButtonId).text.toString()
            }

            else -> {
                toast("Invalid owner type")
                return
            }
        }

        // 🔥 Important fields
        data["ownerId"] = uid
        data["status"] = "draft"

        db.collection("listings")
            .add(data)
            .addOnSuccessListener { docRef ->

                toast("Step 1 saved successfully")

                val intent = Intent(this, ActivityOwnerAddNewList2::class.java)
                intent.putExtra("LISTING_ID", docRef.id)
                intent.putExtra("OWNER_TYPE", ownerType)
                startActivity(intent)
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
