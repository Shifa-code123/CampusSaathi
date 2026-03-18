package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostGridAdapter(
    private var postList: ArrayList<Post>
) : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

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

        holder.txtHeading.text = post.heading
        holder.txtCaption.text = post.caption

        // 🔥 Image load (safe)
        if (!post.img.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.img)
                .placeholder(R.drawable.default_avatar)
                .into(holder.imgPost)
        } else {
            holder.imgPost.setImageResource(R.drawable.default_avatar)
        }

        // ❤️ Like button (toggle)
        var isLiked = false

        holder.btnLike.setOnClickListener {
            isLiked = !isLiked

            if (isLiked) {
                holder.btnLike.setImageResource(R.drawable.ic_like)
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_like)
            }
        }

        // 📤 Share button
        holder.btnShare.setOnClickListener {

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${post.heading}\n${post.caption}")
            }

            holder.itemView.context.startActivity(
                Intent.createChooser(intent, "Share via")
            )
        }
    }

    override fun getItemCount(): Int = postList.size

    // 🔥 IMPORTANT: Fragment se data update karne ke liye
    fun updateData(newList: ArrayList<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}