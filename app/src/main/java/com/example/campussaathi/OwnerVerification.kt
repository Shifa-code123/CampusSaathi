package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class OwnerVerification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    private var idProofUri: Uri? = null
    private var ownerProofUri: Uri? = null
    private var ownerType: String? = null

    companion object {
        const val PICK_ID_PROOF = 101
        const val PICK_OWNER_PROOF = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_verification)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val uid = auth.currentUser?.uid ?: return

        layoutRoom = findViewById(R.id.layoutRoomVerification)
        layoutMess = findViewById(R.id.layoutMessVerification)
        layoutTuition = findViewById(R.id.layoutTuitionVerification)

        val btnUploadId = findViewById<Button>(R.id.btnUploadId)
        val btnUploadRoom = findViewById<Button>(R.id.btnUploadRoomProof)
        val btnUploadMess = findViewById<Button>(R.id.btnUploadMessProof)
        val btnUploadTuition = findViewById<Button>(R.id.btnUploadTuitionProof)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitVerification)

        hideAllLayouts()

        // 🔹 Get owner type
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                ownerType = doc.getString("ownerType")

                when (ownerType) {
                    "room_pg" -> layoutRoom.visibility = View.VISIBLE
                    "mess" -> layoutMess.visibility = View.VISIBLE
                    "tuition" -> layoutTuition.visibility = View.VISIBLE
                }
            }


        // 🔹 Upload buttons
        btnUploadId.setOnClickListener { pickFile(PICK_ID_PROOF) }
        btnUploadRoom.setOnClickListener { pickFile(PICK_OWNER_PROOF) }
        btnUploadMess.setOnClickListener { pickFile(PICK_OWNER_PROOF) }
        btnUploadTuition.setOnClickListener { pickFile(PICK_OWNER_PROOF) }

        // 🔹 Submit
        btnSubmit.setOnClickListener {
            if (idProofUri == null || ownerProofUri == null) {
                toast("Please upload ID proof and service proof")
                return@setOnClickListener
            }
            uploadDocuments(uid)
        }
    }

    private fun pickFile(code: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_ID_PROOF -> {
                    idProofUri = data.data
                    toast("ID proof selected")
                }
                PICK_OWNER_PROOF -> {
                    ownerProofUri = data.data
                    toast("Service proof selected")
                }
            }
        }
    }

    private fun uploadDocuments(uid: String) {

        val idRef = storage.reference.child("owner_verification/$uid/id_proof")

        idRef.putFile(idProofUri!!)
            .addOnFailureListener {
                toast("Failed to upload ID proof")
            }
            .addOnSuccessListener {
                idRef.downloadUrl.addOnSuccessListener { idUrl ->

                    val ownerRef =
                        storage.reference.child("owner_verification/$uid/$ownerType/proof")

                    ownerRef.putFile(ownerProofUri!!)
                        .addOnFailureListener {
                            toast("Failed to upload service proof")
                        }
                        .addOnSuccessListener {
                            ownerRef.downloadUrl.addOnSuccessListener { ownerUrl ->
                                saveVerification(uid, idUrl.toString(), ownerUrl.toString())
                            }
                        }
                }
            }
    }

    private fun saveVerification(uid: String, idUrl: String, ownerUrl: String) {

        val verificationData = hashMapOf(
            "uid" to uid,
            "ownerType" to ownerType,
            "idProofUrl" to idUrl,
            "ownerProofUrl" to ownerUrl,
            "status" to "pending",
            "submittedAt" to FieldValue.serverTimestamp()
        )

        db.collection("owner_verifications").document(uid)
            .set(verificationData)
            .addOnSuccessListener {

                db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "verificationSubmitted" to true,
                            "isVerified" to false,
                            "ownerSetupDone" to true
                        )
                    )

                toast("Verification submitted successfully")

                startActivity(
                    Intent(this, ActivityOwnerVerificationInProgress::class.java)
                )
                finish()
            }
            .addOnFailureListener {
                toast("Failed to submit verification")
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
