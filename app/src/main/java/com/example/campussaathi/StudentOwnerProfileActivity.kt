package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ActivityStudentOwnerProfileBinding
import com.example.campussaathi.utils.DrawerManager
import com.google.firebase.firestore.FirebaseFirestore

class StudentOwnerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentOwnerProfileBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentOwnerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Drawer setup
        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.header.tvHeaderTitle.text = "Owner Profile"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        setupFooter()

        val ownerId = intent.getStringExtra("ownerId") ?: return

        loadOwnerInfo(ownerId)
        loadOwnerPosts(ownerId)
    }

    // ---------------- OWNER NAME ----------------

    private fun loadOwnerInfo(ownerId: String) {

        db.collection("owner_verifications")
            .whereEqualTo("uid", ownerId)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->

                if (!docs.isEmpty) {

                    val doc = docs.documents[0]

                    binding.txtOwnerName.text =
                        doc.getString("fullName") ?: "Owner"
                }
            }
    }


    // ---------------- POSTS ----------------

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

                    var url = doc.getString("business_pic")

                    if (url.isNullOrEmpty()) {
                        url = doc.getString("img")
                    }

                    if (!url.isNullOrEmpty()) {

                        Glide.with(binding.profileImage)
                            .load(url)
                            .circleCrop()
                            .into(binding.profileImage)

                        break   // first image only
                    }
                }

                binding.recyclerPosts.layoutManager =
                    LinearLayoutManager(this)

                binding.recyclerPosts.adapter =
                    PostGridAdapter(postList)
            }
    }

    // ---------------- FOOTER ----------------

    private fun setupFooter() {

        binding.csFooter.csFooterHomeContainer.setOnClickListener {

            val intent = Intent(this, StudentActivity::class.java)
            intent.putExtra("OPEN_TAB","HOME")
            startActivity(intent)
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {

            val intent = Intent(this, StudentActivity::class.java)
            intent.putExtra("OPEN_TAB","EXPLORE")
            startActivity(intent)
        }

        binding.csFooter.csFooterNearmeContainer.setOnClickListener {

            val intent = Intent(this, StudentActivity::class.java)
            intent.putExtra("OPEN_TAB","NEARBY")
            startActivity(intent)
        }

        binding.csFooter.csFooterHelpContainer.setOnClickListener {

            val intent = Intent(this, StudentActivity::class.java)
            intent.putExtra("OPEN_TAB","HELP")
            startActivity(intent)
        }
    }
}