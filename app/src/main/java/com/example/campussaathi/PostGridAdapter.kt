package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostGridAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

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

        // Load Cloudinary image
        if (post.img.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(post.img)
                .into(holder.imgPost)
        }

        // Like button
        holder.btnLike.setOnClickListener {
            holder.btnLike.setImageResource(R.drawable.ic_like)
        }

        // Share button
        holder.btnShare.setOnClickListener {

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, post.caption)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}