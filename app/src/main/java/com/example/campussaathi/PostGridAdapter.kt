package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostGridAdapter(
    private var postList: ArrayList<Post>
) : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHeading: TextView = view.findViewById(R.id.txtHeading)
        val txtCaption: TextView = view.findViewById(R.id.txtCaption)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnShare: ImageView = view.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_feed, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val context = holder.itemView.context
        val userId = auth.currentUser?.uid ?: return

        holder.txtHeading.text = post.heading
        holder.txtCaption.text = post.caption

        // 🔥 Image load
        if (!post.img.isNullOrEmpty()) {
            Glide.with(context)
                .load(post.img)
                .placeholder(R.drawable.default_avatar)
                .into(holder.imgPost)
        } else {
            holder.imgPost.setImageResource(R.drawable.default_avatar)
        }

        // ❤️ Like Logic (Toggle)
        if (post.postId.isNotEmpty()) {
            val likeRef = db.collection("likes").document(post.postId)
                .collection("userLikes").document(userId)

            likeRef.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    holder.btnLike.setImageResource(R.drawable.ic_heart) // Using ic_heart as filled
                    holder.btnLike.tag = "liked"
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_like) // Using ic_like as outline
                    holder.btnLike.tag = "unliked"
                }
            }

            holder.btnLike.setOnClickListener {
                if (holder.btnLike.tag == "liked") {
                    likeRef.delete()
                } else {
                    likeRef.set(hashMapOf("likedAt" to System.currentTimeMillis()))
                }
            }
        }

        // 📤 Share Logic
        holder.btnShare.setOnClickListener {
            val shareText = "${post.heading}\n\n${post.caption}\n\nCheck out this image: ${post.img}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, post.heading)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share Post via"))
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updateData(newList: ArrayList<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}