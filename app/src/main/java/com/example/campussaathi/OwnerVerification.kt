package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OwnerVerification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    // 🔹 TEXT INPUTS (THIS WAS MISSING BEFORE)
    private lateinit var etIdProof: EditText
    private lateinit var etServiceProof: EditText

    private var ownerType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_verification)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            toast("User not logged in")
            finish()
            return
        }

        // 🔹 Layouts (YOUR LOGIC KEPT)
        layoutRoom = findViewById(R.id.layoutRoomVerification)
        layoutMess = findViewById(R.id.layoutMessVerification)
        layoutTuition = findViewById(R.id.layoutTuitionVerification)

        // 🔹 EditTexts (CRITICAL FIX)
        etIdProof = findViewById(R.id.etFullName)       // ID proof text
        etServiceProof = findViewById(R.id.etPhone)    // Service proof text

        val btnSubmit = findViewById<Button>(R.id.btnSubmitVerification)

        hideAllLayouts()

        // 🔹 Fetch owner type (UNCHANGED LOGIC)
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                ownerType = doc.getString("ownerType")

                when (ownerType) {
                    "room_pg" -> layoutRoom.visibility = View.VISIBLE
                    "mess" -> layoutMess.visibility = View.VISIBLE
                    "tuition" -> layoutTuition.visibility = View.VISIBLE
                    else -> toast("Owner type not found")
                }
            }
            .addOnFailureListener {
                toast("Failed to load owner type")
            }

        // 🔹 Submit verification (TEXT ONLY)
        btnSubmit.setOnClickListener {

            val idProofText = etIdProof.text.toString().trim()
            val serviceProofText = etServiceProof.text.toString().trim()

            if (idProofText.isEmpty() || serviceProofText.isEmpty()) {
                toast("Please fill all verification details")
                return@setOnClickListener
            }

            submitVerification(uid, idProofText, serviceProofText)
        }
    }

    private fun submitVerification(
        uid: String,
        idProof: String,
        serviceProof: String
    ) {

        val verificationData = hashMapOf(
            "uid" to uid,
            "ownerType" to ownerType,
            "idProofText" to idProof,
            "serviceProofText" to serviceProof,
            "status" to "pending",
            "submittedAt" to FieldValue.serverTimestamp()
        )

        db.collection("owner_verifications")
            .document(uid)
            .set(verificationData)
            .addOnSuccessListener {

                // 🔹 THIS IS WHAT MAKES "Verification in Progress" SHOW
                db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "verificationSubmitted" to true,
                            "isVerified" to false,
                            "ownerSetupDone" to true
                        )
                    )

                toast("Verification submitted")

                startActivity(
                    Intent(this, ActivityOwnerVerificationInProgress::class.java)
                )
                finish()
            }
            .addOnFailureListener {
                toast("Verification failed. Try again.")
            }
    }

    private fun hideAllLayouts() {
        layoutRoom.visibility = View.GONE
        layoutMess.visibility = View.GONE
        layoutTuition.visibility = View.GONE
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
