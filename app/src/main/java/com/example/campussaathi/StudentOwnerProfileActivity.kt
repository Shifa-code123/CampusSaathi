package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.ActivityStudentOwnerProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

class StudentOwnerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentOwnerProfileBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentOwnerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===== HEADER TITLE =====
        binding.header.tvHeaderTitle.text = "OwnerProfile"

        // ===== DRAWER OPEN =====
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // ===== FOOTER SETUP =====
        setupFooter()

        val ownerId = intent.getStringExtra("OWNER_ID") ?: return

        loadOwnerInfo(ownerId)
        loadOwnerPosts(ownerId)
    }

    // ===== LOAD OWNER INFO =====

    private fun loadOwnerInfo(ownerId: String) {

        db.collection("owner_verifications")
            .document(ownerId)
            .get()
            .addOnSuccessListener { doc ->

                binding.txtOwnerName.text =
                    doc.getString("fullName") ?: "Owner"

                val followers = doc.getLong("followers") ?: 0
                binding.txtFollowersCount.text = followers.toString()

                val base64 = doc.getString("proofImageBase64")

                if (!base64.isNullOrEmpty()) {

                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)

                    binding.profileImage.setImageBitmap(bitmap)
                }
            }
    }

    // ===== LOAD POSTS =====

    private fun loadOwnerPosts(ownerId: String) {

        db.collection("posts")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { docs ->

                binding.txtPostsCount.text = docs.size().toString()

                val postList = ArrayList<Post>()

                for (doc in docs) {

                    val post = doc.toObject(Post::class.java)
                    postList.add(post)
                }

                binding.recyclerPosts.layoutManager =
                    LinearLayoutManager(this)

                binding.recyclerPosts.adapter =
                    PostGridAdapter(postList)
            }
    }

    // ===== FOOTER NAVIGATION =====

    private fun setupFooter() {

        binding.csFooter.csFooterHomeContainer.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        binding.csFooter.csFooterNearmeContainer.setOnClickListener {
            startActivity(Intent(this, NearbyActivity::class.java))
        }

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }
}