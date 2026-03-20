package com.example.campussaathi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.R
import com.example.campussaathi.CommentModel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private val list: ArrayList<CommentModel>,
    private val onLongPress: (CommentModel) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userName: TextView = itemView.findViewById(R.id.tvName)
        val comment: TextView = itemView.findViewById(R.id.tvComment)
        val profile: ImageView = itemView.findViewById(R.id.profileimage)

        // ❌ REMOVE THIS (not in XML)
        // val time: TextView = itemView.findViewById(R.id.txtTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val commentData = list[position]

        holder.userName.text = commentData.userName
        holder.comment.text = commentData.commentText

        holder.itemView.setOnLongClickListener {
            onLongPress(commentData)
            true
        }

        db.collection("users")
            .document(commentData.userId)
            .get()
            .addOnSuccessListener { doc ->

                val profileImage = doc.getString("profileImage")

                Glide.with(holder.itemView.context)
                    .load(profileImage)
                    .placeholder(R.drawable.ic_personz)
                    .circleCrop()
                    .into(holder.profile)
            }
    }
}