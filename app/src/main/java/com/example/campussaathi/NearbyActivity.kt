package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class NearbyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)

        val recyclerView = findViewById<RecyclerView>(R.id.nearbyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        FirebaseFirestore.getInstance()
            .collection("nearby_places")
            .get()
            .addOnSuccessListener { result ->

                val list = mutableListOf<Pair<String, NearbyPlace>>()

                for (doc in result) {
                    val place = doc.toObject(NearbyPlace::class.java)
                    list.add(Pair(doc.id, place)) // REAL documentId
                }

                recyclerView.adapter = NearbyAdapter(list) { docId ->
                    val intent = Intent(this, NearbyDetailActivity::class.java)
                    intent.putExtra("doc_id", docId)
                    startActivity(intent)
                }
            }
    }
}