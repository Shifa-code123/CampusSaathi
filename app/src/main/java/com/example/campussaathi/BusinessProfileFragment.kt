package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.Toolbar

class BusinessProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var headerProfile: ImageView
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

    private val PICK_IMAGE_REQUEST = 1001
    private val CAMERA_REQUEST = 1002

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_business_profile, container, false)

        initViews(view)
        setupTabs()
        loadProfileData()

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityOwnerProfile::class.java))
        }

        profileImage.setOnClickListener { showImageSourceDialog() }
        fabAddPost.setOnClickListener { showImageSourceDialog() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 Drawer setup (same as ReviewFragment)
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawerLayout)
        val drawerView = view.findViewById<View>(R.id.customDrawer)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_menu)

        if (drawerLayout != null && drawerView != null && toolbar != null) {

            toolbar.setNavigationOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }

            val drawerHelper = OwnerCustomDrawerHelper(
                requireActivity(),
                drawerLayout,
                drawerView
            )
            drawerHelper.setup()
        }
    }

    private fun initViews(view: View) {

        profileImage = view.findViewById(R.id.profileImage)
        headerProfile = view.findViewById(R.id.headerProfile)

        txtPostsCount = view.findViewById(R.id.txtPostsCount)
        txtFollowersCount = view.findViewById(R.id.txtFollowersCount)
        txtOwnerName = view.findViewById(R.id.txtOwnerName)
        txtBio = view.findViewById(R.id.txtBio)

        fabAddPost = view.findViewById(R.id.fabAddPost)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
    }

    private fun loadProfileData() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                txtBio.text = doc.getString("bio") ?: "Add your bio..."

                loadBusinessProfileImage(uid)
                loadPostsCount(uid)
            }
    }

    private fun loadBusinessProfileImage(uid: String) {

        db.collection("posts")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val url = doc.getString("business_pic")

                if (!url.isNullOrEmpty()) {

                    Thread {
                        try {
                            val bitmap = BitmapFactory.decodeStream(URL(url).openStream())

                            activity?.runOnUiThread {
                                profileImage.setImageBitmap(bitmap)
                                headerProfile.setImageBitmap(bitmap)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.start()
                }
            }
    }

    private fun loadPostsCount(uid: String) {

        db.collection("posts")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener {
                txtPostsCount.text = it.size().toString()
            }
    }

    private fun showImageSourceDialog() {

        val options = arrayOf("Upload from Gallery", "Open Camera")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Option")
            .setItems(options) { _, which ->
                if (which == 0) openGallery() else openCamera()
            }
            .show()
    }

    private fun openGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        var bitmap: Bitmap? = null

        when (requestCode) {

            PICK_IMAGE_REQUEST -> {
                val uri = data?.data
                val stream = requireContext().contentResolver.openInputStream(uri!!)
                bitmap = BitmapFactory.decodeStream(stream)
            }

            CAMERA_REQUEST -> {
                bitmap = data?.extras?.get("data") as Bitmap
            }
        }

        bitmap?.let {
            profileImage.setImageBitmap(it)
            uploadImageToCloudinary(it)
        }
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap) {

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)

        val byteArray = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                byteArray.toRequestBody("image/*".toMediaType())
            )
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
                val url = JSONObject(body).getString("secure_url")

                saveBusinessPic(url)
            }
        })
    }

    private fun saveBusinessPic(url: String) {

        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "ownerId" to uid,
            "business_pic" to url
        )

        db.collection("posts")
            .document(uid)
            .set(data)
            .addOnSuccessListener {

                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Profile picture updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
}