package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class OwnerVerification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    private lateinit var etIdProof: EditText
    private lateinit var etServiceProof: EditText

    // 🔥 NEW IMAGE VARIABLES
    private lateinit var btnBrowseProof: Button
    private lateinit var imgProofPreview: ImageView
    private lateinit var txtSelectedFile: TextView
    private var imageUri: Uri? = null

    private var ownerType: String? = null

    // 🔥 IMAGE PICKER
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                txtSelectedFile.text = "File Selected"
                imgProofPreview.visibility = View.VISIBLE
                imgProofPreview.setImageURI(it)
            }
        }

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

        layoutRoom = findViewById(R.id.layoutRoomVerification)
        layoutMess = findViewById(R.id.layoutMessVerification)
        layoutTuition = findViewById(R.id.layoutTuitionVerification)

        etIdProof = findViewById(R.id.etFullName)
        etServiceProof = findViewById(R.id.etPhone)

        // 🔥 Bind Image Views
        btnBrowseProof = findViewById(R.id.btnBrowseProof)
        imgProofPreview = findViewById(R.id.imgProofPreview)
        txtSelectedFile = findViewById(R.id.txtSelectedFile)

        val btnSubmit = findViewById<Button>(R.id.btnSubmitVerification)

        hideAllLayouts()

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

        btnBrowseProof.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSubmit.setOnClickListener {

            val idProofText = etIdProof.text.toString().trim()
            val serviceProofText = etServiceProof.text.toString().trim()

            if (idProofText.isEmpty() || serviceProofText.isEmpty()) {
                toast("Please fill all verification details")
                return@setOnClickListener
            }

            if (imageUri == null) {
                toast("Please upload proof image")
                return@setOnClickListener
            }

            submitVerification(uid, idProofText, serviceProofText)
        }
    }

    // 🔥 Convert Image To Base64 (Compressed)
    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()

            // compress to reduce Firestore size
            bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                35,
                outputStream
            )

            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun submitVerification(
        uid: String,
        idProof: String,
        serviceProof: String
    ) {

        val base64Image = convertImageToBase64(imageUri!!)

        if (base64Image == null) {
            toast("Image conversion failed")
            return
        }

        val verificationData = hashMapOf(
            "uid" to uid,
            "ownerType" to ownerType,
            "fullName" to idProof,
            "phone" to serviceProof,
            "proofImageBase64" to base64Image,   // 🔥 STORED IN FIRESTORE
            "status" to "pending",
            "submittedAt" to FieldValue.serverTimestamp()
        )

        db.collection("owner_verifications")
            .document(uid)
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
