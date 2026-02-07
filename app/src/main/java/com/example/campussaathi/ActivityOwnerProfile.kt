package com.example.campussaathi.owner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campussaathi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ActivityOwnerProfile : AppCompatActivity() {

    // Views
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

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var isEditMode = false

    // 🔵 Image picker (modern way)
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadProfileImage(it) }
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

                doc.getString("profileImage")?.let {
                    Glide.with(this).load(it).into(imgProfile)
                }
            }
    }

    // 🟡 EDIT MODE
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

    // 🟢 SAVE PROFILE
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

    // 🟣 UPLOAD IMAGE
    private fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->

                    db.collection("users").document(uid)
                        .update("profileImage", downloadUri.toString())

                    Glide.with(this)
                        .load(downloadUri)
                        .into(imgProfile)

                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
