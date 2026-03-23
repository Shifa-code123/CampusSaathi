package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL

class BusinessProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var txtPostsCount: TextView
    private lateinit var txtFollowersCount: TextView
    private lateinit var txtOwnerName: TextView
    private lateinit var txtBio: TextView
    private lateinit var fabAddPost: FloatingActionButton
    private lateinit var btnEditProfile: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Separate Request Codes to prevent logic cross-triggering
    private val PICK_PROFILE_IMAGE = 1001
    private val CAMERA_PROFILE_IMAGE = 1002
    private val PICK_POST_IMAGE = 1003
    private val CAMERA_POST_IMAGE = 1004

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_business_profile, container, false)
        initViews(view)
        setupTabs()
        loadProfileData()

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityOwnerProfile::class.java))
        }

        profileImage.setOnClickListener {
            showProfileImageSourceDialog()
        }

        fabAddPost.setOnClickListener {
            showPostImageSourceDialog()
        }

        return view
    }

    private fun initViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        txtPostsCount = view.findViewById(R.id.txtPostsCount)
        txtFollowersCount = view.findViewById(R.id.txtFollowersCount)
        txtOwnerName = view.findViewById(R.id.txtOwnerName)
        txtBio = view.findViewById(R.id.txtBio)
        fabAddPost = view.findViewById(R.id.fabAddPost)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
    }

    private fun setupTabs() {
        val adapter = ProfilePagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_grid)
                1 -> tab.setIcon(R.drawable.ic_services)
            }
        }.attach()
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                txtBio.text = doc.getString("bio") ?: "Add your bio..."
                loadBusinessProfileImage(uid)
                loadPostsCount(uid)
                loadFollowersCount(uid)
            }
    }

    private fun loadBusinessProfileImage(uid: String) {
        db.collection("posts").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val url = doc.getString("business_pic")
                    if (!url.isNullOrEmpty()) {
                        Thread {
                            try {
                                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                                activity?.runOnUiThread {
                                    profileImage.setImageBitmap(bitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.start()
                    }
                }
            }
    }

    private fun loadPostsCount(uid: String) {
        db.collection("posts").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener { txtPostsCount.text = it.size().toString() }
    }

    private fun loadFollowersCount(uid: String) {
        // Real-time listener for followers count
        db.collection("followers").document(uid)
            .collection("userFollowers")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                txtFollowersCount.text = snapshot.size().toString()
            }
    }

    // --- Profile Image Logic ---
    private fun showProfileImageSourceDialog() {
        val options = arrayOf("Upload from Gallery", "Open Camera")
        AlertDialog.Builder(requireContext())
            .setTitle("Update Profile Photo")
            .setItems(options) { _, which ->
                if (which == 0) openProfileGallery()
                else openProfileCamera()
            }.show()
    }

    private fun openProfileGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_PROFILE_IMAGE)
    }

    private fun openProfileCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_PROFILE_IMAGE)
    }

    // --- Post Upload Logic ---
    private fun showPostImageSourceDialog() {
        val options = arrayOf("Upload from Gallery", "Open Camera")
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Post")
            .setItems(options) { _, which ->
                if (which == 0) openPostGallery()
                else openPostCamera()
            }.show()
    }

    private fun openPostGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_POST_IMAGE)
    }

    private fun openPostCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_POST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            PICK_PROFILE_IMAGE, CAMERA_PROFILE_IMAGE -> {
                val bitmap = if (requestCode == PICK_PROFILE_IMAGE) {
                    data?.data?.let { uri ->
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream)
                    }
                } else {
                    data?.extras?.get("data") as? Bitmap
                }
                bitmap?.let {
                    profileImage.setImageBitmap(it)
                    uploadImageToCloudinary(it)
                }
            }
            PICK_POST_IMAGE, CAMERA_POST_IMAGE -> {
                val bitmap = if (requestCode == PICK_POST_IMAGE) {
                    data?.data?.let { uri ->
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream)
                    }
                } else {
                    data?.extras?.get("data") as? Bitmap
                }
                bitmap?.let {
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val byteArray = stream.toByteArray()
                    
                    val fragment = CreatePostFragment().apply {
                        arguments = Bundle().apply {
                            putByteArray("image_data", byteArray)
                        }
                    }
                    
                    parentFragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", byteArray.toRequestBody("image/*".toMediaType()))
            .addFormDataPart("upload_preset", "campussaathi_upload")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val url = JSONObject(body!!).getString("secure_url")
                saveBusinessPic(url)
            }
        })
    }

    private fun saveBusinessPic(url: String) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf("ownerId" to uid, "business_pic" to url)
        db.collection("posts").document(uid).set(data)
            .addOnSuccessListener {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun openServicesTab() {
        viewPager.currentItem = 1
    }
}
