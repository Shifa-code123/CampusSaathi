package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class NearbyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_detail)

        val docId = intent.getStringExtra("doc_id") ?: return

        FirebaseFirestore.getInstance()
            .collection("nearby_places")
            .document(docId)
            .get()
            .addOnSuccessListener { doc ->

                val place = doc.toObject(NearbyPlace::class.java) ?: return@addOnSuccessListener

                findViewById<TextView>(R.id.detailName).text = place.name
                findViewById<TextView>(R.id.detailAbout).text = place.about
                findViewById<TextView>(R.id.detailDistance).text = place.distance

                if (place.photos.isNotEmpty()) {
                    Glide.with(this)
                        .load(place.photos[0])
                        .into(findViewById<ImageView>(R.id.detailImage))
                }

                findViewById<Button>(R.id.openMapBtn).setOnClickListener {
                    val uri = Uri.parse("geo:${place.latitude},${place.longitude}")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }

                findViewById<Button>(R.id.callBtn).setOnClickListener {
                    if (place.contacts.isNotEmpty()) {
                        startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.contacts[0]}"))
                        )
                    }
                }
            }
    }
}