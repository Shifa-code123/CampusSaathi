package com.example.campussaathi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.ActivityCategoryListBinding
import com.google.firebase.firestore.FirebaseFirestore

class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding

    private val db = FirebaseFirestore.getInstance()

    private val list = ArrayList<CityHelpModel>()
    private lateinit var adapter: CityHelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)




        val category = intent.getStringExtra("category") ?: ""

        adapter = CityHelpAdapter(list)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter

        loadData(category)

        binding.btnBack.setOnClickListener {
            finish()
        }

    }



    private fun loadData(category: String) {

        db.collection("cityhelp")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener {

                list.clear()

                for (doc in it.documents) {

                    val item = doc.toObject(CityHelpModel::class.java)

                    if (item != null) {
                        list.add(item)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }

}