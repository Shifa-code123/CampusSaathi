package com.example.campussaathi

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ActivityOwnerProfile : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtRole: TextView
    private lateinit var txtPhone: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView

    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var layoutVerified: LinearLayout
    private lateinit var itemEdit: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var isEditMode = false

    // 🔵 Image Picker
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { convertAndSaveProfileImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_profile)

        bindViews()
        loadProfileData()

        itemEdit.setOnClickListener {
            if (!isEditMode) enableEditMode() else saveProfileChanges()
        }

        imgProfile.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    private fun bindViews() {
        imgProfile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)
        txtRole = findViewById(R.id.txtRole)
        txtPhone = findViewById(R.id.txtPhone)
        txtEmail = findViewById(R.id.txtEmail)
        txtAddress = findViewById(R.id.txtAddress)

        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)

        layoutVerified = findViewById(R.id.layoutVerified)
        itemEdit = findViewById(R.id.itemEdit)
    }

    // 🔵 LOAD PROFILE
    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                txtName.text = doc.getString("name")
                txtRole.text = doc.getString("role")
                txtPhone.text = doc.getString("phone")
                txtEmail.text = doc.getString("email")
                txtAddress.text = doc.getString("location")

                layoutVerified.visibility =
                    if (doc.getBoolean("verified") == true) View.VISIBLE else View.GONE

                try {
                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgProfile.setImageBitmap(bitmap)
                    } else {
                        imgProfile.setImageResource(R.drawable.default_avatar)
                    }

                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.default_avatar)
                }

            }
    }

    private fun enableEditMode() {
        isEditMode = true

        edtPhone.setText(txtPhone.text)
        edtAddress.setText(txtAddress.text)

        txtPhone.visibility = View.GONE
        txtAddress.visibility = View.GONE

        edtPhone.visibility = View.VISIBLE
        edtAddress.visibility = View.VISIBLE

        Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return

        val updateMap = mapOf(
            "phone" to edtPhone.text.toString().trim(),
            "location" to edtAddress.text.toString().trim()
        )

        db.collection("users").document(uid)
            .update(updateMap)
            .addOnSuccessListener {

                txtPhone.text = edtPhone.text
                txtAddress.text = edtAddress.text

                txtPhone.visibility = View.VISIBLE
                txtAddress.visibility = View.VISIBLE

                edtPhone.visibility = View.GONE
                edtAddress.visibility = View.GONE

                isEditMode = false
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 Convert profile image to Base64 and save
    private fun convertAndSaveProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                35,
                outputStream
            )

            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            db.collection("users").document(uid)
                .update("profileImageBase64", base64Image)
                .addOnSuccessListener {

                    val bytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmapDecoded =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    imgProfile.setImageBitmap(bitmapDecoded)

                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show()
        }
    }
}
