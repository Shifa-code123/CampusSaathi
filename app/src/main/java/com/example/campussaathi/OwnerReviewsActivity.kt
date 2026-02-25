package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class OwnerReviewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_owner_reviews)

        val recycler = findViewById<RecyclerView>(R.id.recyclerReviews)

        recycler.layoutManager = LinearLayoutManager(this)
    }
}