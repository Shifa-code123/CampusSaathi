package com.example.campussaathi

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(private var reviewList: List<ReviewModel>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.binding.apply {
            tvStudentName.text = review.userName.ifEmpty { "Student" }
            rbItemRating.rating = review.rating.toFloat()
            tvReviewText.text = review.comment.ifEmpty { "No review available" }
            
            // Format timestamp
            if (review.timestamp > 0) {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                tvReviewTime.text = sdf.format(Date(review.timestamp))
            } else {
                tvReviewTime.text = ""
            }

            // Decode and set Profile Image from Base64
            if (review.profileImageBase64.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(review.profileImageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        ivStudentProfile.setImageBitmap(bitmap)
                    } else {
                        ivStudentProfile.setImageResource(R.drawable.ic_profile)
                    }
                } catch (e: Exception) {
                    ivStudentProfile.setImageResource(R.drawable.ic_profile)
                }
            } else {
                ivStudentProfile.setImageResource(R.drawable.ic_profile)
            }
            
            btnReply.setOnClickListener {
                // UI only, no backend needed
            }
        }
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateData(newList: List<ReviewModel>) {
        reviewList = newList
        notifyDataSetChanged()
    }
}
