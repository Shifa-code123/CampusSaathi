package com.example.campussaathi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddServiceFragment : Fragment() {

    private lateinit var etServiceName: EditText
    private lateinit var etContact: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnGps: ImageView
    private lateinit var txtDistance: TextView
    private lateinit var spinnerType: Spinner
    private lateinit var btnAddPhoto: Button
    private lateinit var imgPreviewContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    private val imageBitmapList = mutableListOf<Bitmap>()
    private val imageUrlList = mutableListOf<String>()

    private var selectedLatLng: LatLng? = null
    private var distanceKm: Double = 0.0

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val campusLatLng = LatLng(20.7074, 76.5680)

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermission = 101

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.forEach { uri ->
                if (imageBitmapList.size >= 5) {
                    Toast.makeText(requireContext(), "Max 5 photos allowed", Toast.LENGTH_SHORT).show()
                    return@forEach
                }
                val stream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                imageBitmapList.add(bitmap)
                addPreview(bitmap)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                if (imageBitmapList.size >= 5) {
                    Toast.makeText(requireContext(), "Max 5 photos allowed", Toast.LENGTH_SHORT).show()
                    return@let
                }
                imageBitmapList.add(it)
                addPreview(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etServiceName = view.findViewById(R.id.etServiceName)
        etContact = view.findViewById(R.id.etContact)
        etDescription = view.findViewById(R.id.etDescription)
        btnSubmit = view.findViewById(R.id.btnSubmitService)
        btnGps = view.findViewById(R.id.btnGps)
        txtDistance = view.findViewById(R.id.txtDistance)
        spinnerType = view.findViewById(R.id.spinnerService)
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto)
        imgPreviewContainer = view.findViewById(R.id.imgPreviewContainer)
        
        // Add a progress bar to the layout if possible, or use a simple Toast/Dialog.
        // For now, I'll just use the submit button state or a simple dialog.
        
        btnAddPhoto.setOnClickListener { showImageSourceDialog() }
        btnGps.setOnClickListener { getCurrentLocation() }
        btnSubmit.setOnClickListener { 
            if (validateFields()) {
                showConfirmationDialog()
            }
        }
    }

    private fun validateFields(): Boolean {
        val name = etServiceName.text.toString()
        val contact = etContact.text.toString()

        if (name.isEmpty() || contact.isEmpty()) {
            Toast.makeText(requireContext(), "Fill required fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (imageBitmapList.isEmpty()) {
            Toast.makeText(requireContext(), "Add photos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Add Service")
            .setMessage("Are you sure you want to add this service?")
            .setPositiveButton("YES") { _, _ ->
                submitService()
            }
            .setNegativeButton("NO", null)
            .show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Upload from Gallery", "Open Camera")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Option")
            .setItems(options) { _, which ->
                if (which == 0) galleryLauncher.launch("image/*")
                else cameraLauncher.launch(null)
            }
            .show()
    }

    private fun addPreview(bitmap: Bitmap) {
        val img = ImageView(requireContext())
        img.setImageBitmap(bitmap)
        img.layoutParams = LinearLayout.LayoutParams(200, 200)
        img.setPadding(8, 8, 8, 8)
        imgPreviewContainer.addView(img)
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermission
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    selectedLatLng = latLng
                    calculateDistance(latLng)
                    Toast.makeText(requireContext(), "Location detected", Toast.LENGTH_SHORT).show()
                    val uri = Uri.parse("geo:${it.latitude},${it.longitude}")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
    }

    private fun calculateDistance(dest: LatLng) {
        val result = FloatArray(1)
        Location.distanceBetween(
            campusLatLng.latitude,
            campusLatLng.longitude,
            dest.latitude,
            dest.longitude,
            result
        )
        distanceKm = result[0] / 1000.0
        txtDistance.text = "Distance from campus: %.2f km".format(distanceKm)
    }

    private fun uploadImage(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "image.jpg",
                byteArray.toRequestBody("image/*".toMediaType())
            )
            .addFormDataPart("upload_preset", "campussaathi_upload")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    val url = JSONObject(body).getString("secure_url")
                    activity?.runOnUiThread {
                        onSuccess(url)
                    }
                }
            }
        })
    }

    private fun submitService() {
        val name = etServiceName.text.toString()
        val contact = etContact.text.toString()
        val desc = etDescription.text.toString()
        val selectedType = spinnerType.selectedItem.toString()

        val uid = auth.currentUser?.uid ?: return

        btnSubmit.isEnabled = false
        Toast.makeText(requireContext(), "Uploading images, please wait...", Toast.LENGTH_LONG).show()

        imageUrlList.clear()
        var uploadedCount = 0

        imageBitmapList.forEach { bitmap ->
            uploadImage(bitmap) { url ->
                imageUrlList.add(url)
                uploadedCount++

                if (uploadedCount == imageBitmapList.size) {
                    val data = hashMapOf(
                        "ownerId" to uid,
                        "serviceName" to name,
                        "contact" to contact,
                        "description" to desc,
                        "category" to selectedType,
                        "photos" to imageUrlList,
                        "latitude" to selectedLatLng?.latitude,
                        "longitude" to selectedLatLng?.longitude,
                        "distanceFromCampus" to distanceKm,
                        "timestamp" to System.currentTimeMillis(),
                        "status" to "pending"
                    )

                    db.collection("services")
                        .add(data)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Service submitted for approval", Toast.LENGTH_SHORT).show()

                            // Set state in SharedPreferences
                            val sharedPref = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                            sharedPref.edit().putBoolean("isServicePending", true).apply()

                            // Navigate to Success Fragment by recreating pager
                            (activity as? OwnerMainActivity)?.let { mainActivity ->
                                mainActivity.recreatePager()
                                mainActivity.navigateTo(R.id.nav_add)
                            }
                        }
                        .addOnFailureListener {
                            btnSubmit.isEnabled = true
                            Toast.makeText(requireContext(), "Failed to add service", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
