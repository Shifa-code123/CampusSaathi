package com.example.campussaathi

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditServicesActivity : AppCompatActivity() {

    private lateinit var etServiceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etContact: EditText
    private lateinit var photoContainer: LinearLayout
    private lateinit var btnUpdate: Button
    private lateinit var btnChangePhotos: Button

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private var serviceDocId: String? = null
    private var photoUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_services)

        etServiceName = findViewById(R.id.etServiceName)
        etDescription = findViewById(R.id.etDescription)
        etContact = findViewById(R.id.etContact)
        photoContainer = findViewById(R.id.photoContainer)
        btnUpdate = findViewById(R.id.btnUpdateService)
        btnChangePhotos = findViewById(R.id.btnChangePhotos)

        loadService()

        btnUpdate.setOnClickListener {
            updateService()
        }

        btnChangePhotos.setOnClickListener {
            Toast.makeText(this,"Implement your Cloudinary photo picker here",Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadService() {

        if (uid == null) return

        db.collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { query ->

                if (!query.isEmpty) {

                    val doc = query.documents[0]

                    serviceDocId = doc.id

                    etServiceName.setText(doc.getString("serviceName"))
                    etDescription.setText(doc.getString("description"))
                    etContact.setText(doc.getString("contact"))

                    val photos = doc.get("photos") as? List<String>

                    photos?.let {

                        photoUrls.clear()
                        photoUrls.addAll(it)

                        displayPhotos()
                    }
                }
            }
    }

    private fun displayPhotos() {

        photoContainer.removeAllViews()

        for (url in photoUrls) {

            val imageView = ImageView(this)

            val params = LinearLayout.LayoutParams(250,250)
            params.setMargins(10,0,10,0)

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            Picasso.get()
                .load(url)
                .into(imageView)

            photoContainer.addView(imageView)
        }
    }

    private fun updateService() {

        val name = etServiceName.text.toString()
        val description = etDescription.text.toString()
        val contact = etContact.text.toString()

        if (serviceDocId == null) return

        val data = hashMapOf(
            "serviceName" to name,
            "description" to description,
            "contact" to contact,
            "photos" to photoUrls
        )

        db.collection("services")
            .document(serviceDocId!!)
            .update(data as Map<String, Any>)
            .addOnSuccessListener {

                Toast.makeText(this,"Service Updated",Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}