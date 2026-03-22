package com.example.campussaathi.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.R
import com.example.campussaathi.CommentModel

class CommentAdapter(
    private val list: ArrayList<CommentModel>,
    private val onLongPress: (CommentModel) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvName)
        val comment: TextView = itemView.findViewById(R.id.tvComment)
        val profile: ImageView = itemView.findViewById(R.id.profileimage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val data = list[position]

        holder.userName.text = data.userName
        holder.comment.text = data.commentText

        holder.itemView.setOnLongClickListener {
            onLongPress(data)
            true
        }

        // 🔥 BASE64 IMAGE LOAD
        if (data.userImage.isNotEmpty()) {
            val bytes = Base64.decode(data.userImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.profile.setImageBitmap(bitmap)
        } else {
            holder.profile.setImageResource(R.drawable.ic_personz)
        }
    }
}