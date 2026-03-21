package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.ItemReviewBinding

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
            tvStudentName.text = review.studentName
            rbItemRating.rating = review.rating.toFloat()
            tvReviewText.text = review.reviewText.ifEmpty { "No review text provided." }
            
            // Dummy time as requested
            tvReviewTime.text = "1 hour ago"
            
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
