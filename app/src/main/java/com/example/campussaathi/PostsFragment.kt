package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostsFragment : Fragment(R.layout.fragment_posts) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var adapter: PostGridAdapter

    private val postList = ArrayList<Post>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerPosts)
        emptyLayout = view.findViewById(R.id.emptyLayout)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)

        adapter = PostGridAdapter(postList)
        recyclerView.adapter = adapter

        loadPosts()
    }

    private fun loadPosts() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { documents ->

                postList.clear()

                for (doc in documents) {
                    val post = doc.toObject(Post::class.java)
                    postList.add(post)
                }

                adapter.notifyDataSetChanged()

                // Show empty layout if no posts
                if (postList.isEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }
}